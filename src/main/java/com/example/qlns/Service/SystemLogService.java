package com.example.qlns.Service;

import com.example.qlns.Entity.SystemLog;
import com.example.qlns.Entity.User;
import com.example.qlns.Repository.SystemLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {

    private final SystemLogRepository logRepo;

    public SystemLogService(SystemLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    public void log(User user, String action, String description) {
        logRepo.save(new SystemLog(user, action, description));
    }

    public void log(String action, String description) {
        logRepo.save(new SystemLog(null, action, description));
    }
}
