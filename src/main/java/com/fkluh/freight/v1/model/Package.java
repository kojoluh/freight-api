package com.fkluh.freight.v1.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Package {
    @Id
    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String recipientPostcode;

    @Column(nullable = false)
    private LocalDate estimatedDeliveryDate;

    private LocalDate actualDeliveryDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatusEnum status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}