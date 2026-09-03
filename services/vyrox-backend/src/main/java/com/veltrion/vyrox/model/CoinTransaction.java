package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coin_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private CoinWallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoinTransactionType type;

    @Column(nullable = false)
    private Integer amount; // positive for earn, negative for redeem

    private String description;
    private String referenceId; // Order ID or campaign

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
