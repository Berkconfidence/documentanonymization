package com.example.documentanonymization.controller;

import com.example.documentanonymization.dto.MessageDto;
import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Message;
import com.example.documentanonymization.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    public MessageController() {}

    @GetMapping
    public List<MessageDto> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/email/{email}")
    public List<MessageDto> getMessageByEmail(@PathVariable String email) {
        return messageService.getMessageByEmail(email);
    }

    @PostMapping("/create")
    public ResponseEntity<Message> createMessage(@RequestParam("email") String email, @RequestParam("content") String content) {
        try {
            Message message = messageService.createMessage(email, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    @PostMapping("/admincreate")
    public ResponseEntity<Message> createAdminMessage(@RequestParam("email") String email, @RequestParam("content") String content) {
        try {
            Message message = messageService.createAdminMessage(email, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
