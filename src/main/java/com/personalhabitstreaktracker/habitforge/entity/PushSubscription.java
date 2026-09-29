package com.personalhabitstreaktracker.habitforge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, length = 512)
    private String p256dhKey;

    @Column(nullable = false, length = 256)
    private String auth;

    protected PushSubscription() {
    }

    public PushSubscription(String endpoint, String p256dhKey, String auth) {
        this.endpoint = endpoint;
        this.p256dhKey = p256dhKey;
        this.auth = auth;
    }

    public Long getId() {
        return id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getP256dhKey() {
        return p256dhKey;
    }

    public String getAuth() {
        return auth;
    }

    public void updateKeys(String p256dhKey, String auth) {
        this.p256dhKey = p256dhKey;
        this.auth = auth;
    }
}