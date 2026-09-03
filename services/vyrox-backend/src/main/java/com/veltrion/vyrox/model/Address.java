package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    private String mobile;
    private String street;
    private String locality;
    private String city;
    private String state;
    private String pincode;
    private String landmark;
    private String addressType; // HOME, WORK, OTHER
    private boolean isDefault;
    private Double latitude;
    private Double longitude;
}
