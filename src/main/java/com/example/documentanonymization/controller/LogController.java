package com.example.documentanonymization.controller;

import com.example.documentanonymization.entity.Log;
import com.example.documentanonymization.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;

    public LogController() {}

    @GetMapping
    public List<Log> getAllLogs() {
        return logService.getAllLogs();
    }

}
