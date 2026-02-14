package com.example.carsharing.model;

import com.example.carsharing.model.enums.PaymentStatus;
import com.example.carsharing.model.enums.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE payments SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted=false")
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Column(nullable = false, length = 512)
    private String sessionUrl;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal amountToPay;

    @Setter(AccessLevel.NONE)
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
