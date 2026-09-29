package com.personalhabitstreaktracker.habitforge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

@Service
public class DesktopNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(DesktopNotificationService.class);

    private final boolean enabled;
    private TrayIcon trayIcon;

    public DesktopNotificationService(
            @Value("${habit.desktop-notification.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    public boolean sendReminderNotification(String habitName, String message) {
        if (!enabled) {
            logger.info("[REMINDER] Desktop notification disabled by configuration: {}", habitName);
            return false;
        }

        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            logger.warn("[REMINDER] Desktop notification unavailable: no interactive Windows tray session");
            return false;
        }

        try {
            ensureTrayIcon();
            trayIcon.displayMessage("HabitForge Reminder", message, TrayIcon.MessageType.INFO);
            logger.info("[REMINDER] Desktop notification sent: {}", habitName);
            return true;
        } catch (Exception exception) {
            logger.warn("[REMINDER] Desktop notification failed: {}", habitName, exception);
            return false;
        }
    }

    private synchronized void ensureTrayIcon() throws Exception {
        if (trayIcon != null) {
            return;
        }

        Image image = createTrayImage();
        trayIcon = new TrayIcon(image, "HabitForge");
        trayIcon.setImageAutoSize(true);
        SystemTray.getSystemTray().add(trayIcon);
    }

    private Image createTrayImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(49, 113, 91));
        graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
        graphics.setColor(Color.WHITE);
        graphics.drawString("H", 4, 12);
        graphics.dispose();
        return image;
    }
}