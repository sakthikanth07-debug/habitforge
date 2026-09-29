package com.personalhabitstreaktracker.habitforge.service;

import com.personalhabitstreaktracker.habitforge.entity.HabitReminder;
import com.personalhabitstreaktracker.habitforge.entity.PushSubscription;
import com.personalhabitstreaktracker.habitforge.repository.PushSubscriptionRepo;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepo pushSubscriptionRepo;
    private final String publicKey;
    private final String privateKey;
    private final String subject;

    public PushNotificationService(PushSubscriptionRepo pushSubscriptionRepo,
                                   @Value("${habit.push.public-key:}") String publicKey,
                                   @Value("${habit.push.private-key:}") String privateKey,
                                   @Value("${habit.push.subject:mailto:habitforge@example.com}") String subject) {
        this.pushSubscriptionRepo = pushSubscriptionRepo;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.subject = subject;
        if (isConfigured() && Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public boolean isConfigured() {
        return !publicKey.isBlank() && !privateKey.isBlank();
    }

    @Transactional
    public void saveSubscription(String endpoint, String p256dhKey, String auth) {
        pushSubscriptionRepo.findByEndpoint(endpoint)
                .ifPresentOrElse(existing -> {
                    existing.updateKeys(p256dhKey, auth);
                    pushSubscriptionRepo.save(existing);
                }, () -> pushSubscriptionRepo.save(new PushSubscription(endpoint, p256dhKey, auth)));
    }

    @Transactional
    public void send(HabitReminder reminder) {
        if (!isConfigured()) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepo.findAll();
        for (PushSubscription subscription : subscriptions) {
            try {
                Subscription webSubscription = new Subscription(
                        subscription.getEndpoint(),
                        new Subscription.Keys(subscription.getP256dhKey(), subscription.getAuth()));
                String payload = "{\"title\":\"HabitForge reminder\",\"body\":\""
                        + escapeJson(reminder.getMessage())
                        + "\",\"url\":\"/habits-page\"}";
                PushService pushService = new PushService(publicKey, privateKey, subject);
                HttpResponse response = pushService.send(new Notification(webSubscription, payload));
                int status = response.getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    pushSubscriptionRepo.delete(subscription);
                }
            } catch (Exception exception) {
                logger.warn("Could not send push reminder to subscription {}", subscription.getId(), exception);
            }
        }
    }

    public Map<String, Object> configuration() {
        return Map.of("enabled", isConfigured(), "publicKey", publicKey);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}