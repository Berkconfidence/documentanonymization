package com.example.documentanonymization.service;

import com.example.documentanonymization.dto.MessageDto;
import com.example.documentanonymization.entity.Message;
import com.example.documentanonymization.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public MessageService() {}

    public List<MessageDto> getMessageByEmail(String email) {
        List<Message> allMessages = messageRepository.findAll();
        return allMessages.stream()
                .filter(message -> message.getReceiverEmail().equals(email) ||
                (message.getReceiverEmail().equals("admin@gmail.com") && message.getSenderEmail().equals(email)))
                .map(message -> {
                    MessageDto dto = new MessageDto();
                    dto.setSenderEmail(message.getSenderEmail());
                    dto.setReceiverEmail(message.getReceiverEmail());
                    dto.setContent(message.getContent());
                    dto.setSentDate(message.getSentDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Message createMessage(String email, String content) {
        Message message = new Message();
        message.setSenderEmail(email);
        message.setReceiverEmail("admin@gmail.com");
        message.setContent(content);
        message.setSentDate(new Date());
        messageRepository.save(message);
        return message;
    }
}
