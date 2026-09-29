package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PushSubscriptionRepo extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpoint(String endpoint);
}