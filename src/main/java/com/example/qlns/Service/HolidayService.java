package com.example.qlns.Service;

import com.example.qlns.DTO.Request.CreateHolidayRequest;
import com.example.qlns.Entity.Holiday;
import com.example.qlns.Exception.BadRequestException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Transactional(readOnly = true)
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Holiday> getHolidaysByMonth(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return holidayRepository.findByDateBetweenOrderByDateAsc(start, end);
    }

    @Transactional
    public Holiday createHoliday(CreateHolidayRequest req) {
        if (req.getDate() == null) throw new BadRequestException("Ngày lễ không được để trống");
        if (req.getName() == null || req.getName().trim().isEmpty()) throw new BadRequestException("Tên ngày lễ không được để trống");
        
        if (holidayRepository.existsByDate(req.getDate())) {
            throw new BadRequestException("Đã tồn tại ngày lễ vào ngày " + req.getDate());
        }

        Holiday holiday = new Holiday();
        holiday.setDate(req.getDate());
        holiday.setName(req.getName().trim());
        holiday.setDescription(req.getDescription());
        return holidayRepository.save(holiday);
    }

    @Transactional
    public Holiday updateHoliday(Long id, CreateHolidayRequest req) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngày lễ này"));

        if (req.getDate() != null && !req.getDate().equals(holiday.getDate())) {
            if (holidayRepository.existsByDate(req.getDate())) {
                throw new BadRequestException("Đã tồn tại một ngày lễ khác vào ngày " + req.getDate());
            }
            holiday.setDate(req.getDate());
        }

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            holiday.setName(req.getName().trim());
        }
        
        if (req.getDescription() != null) {
            holiday.setDescription(req.getDescription());
        }

        return holidayRepository.save(holiday);
    }

    @Transactional
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy ngày lễ này");
        }
        holidayRepository.deleteById(id);
    }
}
