package com.veltrion.vyrox.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "darkstores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Darkstore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;
    private String address;
    private String city;
    private String pincode;

    private Double latitude;
    private Double longitude;

    private boolean active;
    private Integer serviceRadiusKm;
}
