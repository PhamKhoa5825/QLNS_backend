package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateHolidayRequest;
import com.example.qlns.Entity.Holiday;
import com.example.qlns.Service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/holidays")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    // Xem danh sách toàn bộ ngày lễ (Ai cũng xem được)
    @GetMapping
    public ResponseEntity<?> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    // Xem danh sách ngày lễ trong tháng
    @GetMapping("/filter")
    public ResponseEntity<?> getHolidaysByMonth(@RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(holidayService.getHolidaysByMonth(month, year));
    }

    // Thêm ngày lễ (Chỉ dành cho Admin)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createHoliday(@RequestBody CreateHolidayRequest req) {
        Holiday h = holidayService.createHoliday(req);
        return ResponseEntity.ok(h);
    }

    // Sửa mật ngày lễ (Chỉ dành cho Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateHoliday(@PathVariable Long id, @RequestBody CreateHolidayRequest req) {
        Holiday h = holidayService.updateHoliday(id, req);
        return ResponseEntity.ok(h);
    }

    // Xóa ngày lễ (Chỉ dành cho Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.ok().build();
    }
}
