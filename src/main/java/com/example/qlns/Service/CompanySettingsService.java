package com.example.qlns.Service;

import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Repository.CompanySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// =============================================
// TV1 - CompanySettingsService
// =============================================
@Service
public class CompanySettingsService {
    private final CompanySettingsRepository repo;
    private final SystemLogService logService;

    CompanySettingsService(CompanySettingsRepository repo, SystemLogService logService) {
        this.repo = repo;
        this.logService = logService;
    }

    public CompanySettings get() {
        return repo.findById(1L).orElseGet(() -> {
            CompanySettings defaults = new CompanySettings();
            defaults.setCompanyName("Công ty QLNS");
            return repo.save(defaults);
        });
    }

    @Transactional
    public CompanySettings update(CompanySettings req) {
        CompanySettings settings = get();
        if (req.getCompanyName() != null) settings.setCompanyName(req.getCompanyName());
        if (req.getBaseLat() != null) settings.setBaseLat(req.getBaseLat());
        if (req.getBaseLng() != null) settings.setBaseLng(req.getBaseLng());
        if (req.getAllowedRadius() != null) settings.setAllowedRadius(req.getAllowedRadius());
        if (req.getWorkStartTime() != null) settings.setWorkStartTime(req.getWorkStartTime());
        if (req.getMorningEndTime() != null) settings.setMorningEndTime(req.getMorningEndTime());
        if (req.getAfternoonStartTime() != null) settings.setAfternoonStartTime(req.getAfternoonStartTime());
        if (req.getWorkEndTime() != null) settings.setWorkEndTime(req.getWorkEndTime());
        CompanySettings saved = repo.save(settings);
        logService.log("UPDATE", "Đã cập nhật cấu hình hệ thống (Tọa độ/Bán kính/Giờ làm việc)");
        return saved;
    }
}
