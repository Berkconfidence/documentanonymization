package com.example.documentanonymization.service;


import com.example.documentanonymization.entity.Log;
import com.example.documentanonymization.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    public LogService() {}

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    public void createLog(String trackingNumber, String action, String userEmail, String userType) {
        Log log = new Log();
        log.setArticleNumber(trackingNumber);
        log.setAction(action);
        log.setUser(userEmail);
        log.setUserType(userType);
        log.setActionDate(new Date());
        logRepository.save(log);
    }
}
