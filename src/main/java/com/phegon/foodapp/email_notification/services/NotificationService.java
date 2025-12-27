package com.phegon.foodapp.email_notification.services;

import com.phegon.foodapp.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
