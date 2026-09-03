package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final OrderRepository orderRepository;
    private final TrackingLogRepository trackingLogRepository;
    private final DarkstoreRepository darkstoreRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Transactional(readOnly = true)
    public CommerceDto.LiveTrackingDto getLiveTracking(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with order number: " + orderNumber));

        List<TrackingLog> logs = trackingLogRepository.findByOrderIdOrderByTimestampAsc(order.getId());
        List<CommerceDto.TrackingLogItem> logItems = logs.stream().map(l -> CommerceDto.TrackingLogItem.builder()
                .status(l.getStatus() != null ? l.getStatus().name() : "")
                .description(l.getDescription())
                .locationName(l.getLocationName())
                .timestamp(l.getTimestamp())
                .build()).collect(Collectors.toList());

        Darkstore darkstore = darkstoreRepository.findByActiveTrue().stream().findFirst().orElseGet(() ->
                Darkstore.builder()
                        .name("VYROX Darkstore #104 - Koramangala")
                        .latitude(12.9352)
                        .longitude(77.6245)
                        .build()
        );

        DeliveryPartner partner = deliveryPartnerRepository.findByIsAvailableTrue().stream().findFirst().orElseGet(() ->
                DeliveryPartner.builder()
                        .vehicleNumber("KA-01-VX-7789")
                        .vehicleType("Electric Scooter (EV)")
                        .phone("+91 98765 43210")
                        .currentLatitude(12.9450)
                        .currentLongitude(77.6150)
                        .rating(4.9)
                        .completedDeliveries(1240)
                        .build()
        );

        double customerLat = (order.getDeliveryLatitude() != null) ? order.getDeliveryLatitude() : 12.9716;
        double customerLng = (order.getDeliveryLongitude() != null) ? order.getDeliveryLongitude() : 77.5946;

        double driverLat = (partner.getCurrentLatitude() != null) ? partner.getCurrentLatitude() : 12.9500;
        double driverLng = (partner.getCurrentLongitude() != null) ? partner.getCurrentLongitude() : 77.6080;

        // Calculate approximate distance and ETA
        double distanceKm = calculateDistanceKm(driverLat, driverLng, customerLat, customerLng);
        int etaMinutes = Math.max(3, (int) Math.round((distanceKm / 25.0) * 60)); // Avg speed 25 km/h in city

        String statusDesc = switch (order.getStatus()) {
            case PLACED -> "Order placed. Awaiting darkstore confirmation.";
            case CONFIRMED -> "Order confirmed. Items are being packed.";
            case PACKED -> "Package packed and ready for handover.";
            case SHIPPED -> "Dispatched to local delivery hub.";
            case OUT_FOR_DELIVERY -> "Rider is en route with your package!";
            case DELIVERED -> "Delivered at doorstep.";
            case CANCELLED -> "Order has been cancelled.";
            default -> "Processing order.";
        };

        return CommerceDto.LiveTrackingDto.builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .doorstepOtp(order.getDoorstepOtp())
                .customerLat(customerLat)
                .customerLng(customerLng)
                .darkstoreLat(darkstore.getLatitude())
                .darkstoreLng(darkstore.getLongitude())
                .darkstoreName(darkstore.getName())
                .driverLat(driverLat)
                .driverLng(driverLng)
                .driverName("Ramesh Kumar (VYROX Express Rider)")
                .driverPhone(partner.getPhone())
                .driverVehicle(partner.getVehicleType() + " [" + partner.getVehicleNumber() + "]")
                .currentStatusDescription(statusDesc)
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .etaMinutes(order.getStatus() == OrderStatus.DELIVERED ? 0 : etaMinutes)
                .isSimulatedGps(true) // Clearly distinguished simulation vs real GPS
                .logs(logItems)
                .build();
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
