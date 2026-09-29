package com.personalhabitstreaktracker.habitforge;

import com.personalhabitstreaktracker.habitforge.service.DesktopNotificationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DesktopNotificationServiceTest {

    @Test
    void doesNotCrashWhenDesktopSessionIsUnavailable() {
        DesktopNotificationService service = new DesktopNotificationService(true);

        assertDoesNotThrow(() -> service.sendReminderNotification(
                "Morning Exercise",
                "You haven't completed Morning Exercise today."));
    }
}