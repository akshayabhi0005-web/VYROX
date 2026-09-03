package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String vehicleNumber;
    private String vehicleType; // BIKE, SCOOTER, EV_VAN
    private String phone;
    private Double currentLatitude;
    private Double currentLongitude;
    private boolean isAvailable;
    private Double rating;
    private Integer completedDeliveries;
}
