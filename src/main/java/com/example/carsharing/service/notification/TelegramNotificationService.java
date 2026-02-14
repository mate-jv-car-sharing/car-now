package com.example.carsharing.service.notification;

import com.example.carsharing.telegram.RentalNotificationTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final RentalNotificationTelegramBot bot;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Override
    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to send Telegram notification", e);
        }
    }
}
