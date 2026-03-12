package com.yigitusq.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j // Konsola şık loglar basmak için
public class NotificationConsumer {

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(String message) {
        log.info("🔔 YENİ BİLDİRİM YAKALANDI!");
        log.info("✉️ KULLANICIYA GÖNDERİLECEK MESAJ: {}", message);
        log.info("---------------------------------------------------");
    }
}