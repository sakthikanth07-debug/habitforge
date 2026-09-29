package com.personalhabitstreaktracker.habitforge.controller;

import com.personalhabitstreaktracker.habitforge.service.PushNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PushSubscriptionController {

    private final PushNotificationService pushNotificationService;

    public PushSubscriptionController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/push-config")
    public Map<String, Object> getConfiguration() {
        return pushNotificationService.configuration();
    }

    @PostMapping("/push-subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveSubscription(@Valid @RequestBody SubscriptionRequest request) {
        pushNotificationService.saveSubscription(request.endpoint(), request.p256dh(), request.auth());
    }

    public record SubscriptionRequest(@NotBlank String endpoint,
                                      @NotBlank String p256dh,
                                      @NotBlank String auth) {
    }
}