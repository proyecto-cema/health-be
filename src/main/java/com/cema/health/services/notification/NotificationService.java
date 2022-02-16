package com.cema.health.services.notification;

public interface NotificationService {;

    //@Scheduled(cron = "0 15 10 * * ?", zone = "America/Buenos_Aires")
    void notifyAllUsers();

    void sendNotification(String message, String destination);
}
