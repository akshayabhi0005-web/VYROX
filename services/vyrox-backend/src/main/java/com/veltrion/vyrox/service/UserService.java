package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.Address;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.repository.AddressRepository;
import com.veltrion.vyrox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<CommerceDto.AddressDto> getUserAddresses(User user) {
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommerceDto.AddressDto addAddress(User user, CommerceDto.AddressDto dto) {
        List<Address> existing = addressRepository.findByUserId(user.getId());
        boolean isFirst = existing.isEmpty();

        Address address = Address.builder()
                .user(user)
                .name(dto.getName())
                .mobile(dto.getMobile())
                .street(dto.getStreet())
                .locality(dto.getLocality())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .landmark(dto.getLandmark())
                .addressType(dto.getAddressType() != null ? dto.getAddressType() : "HOME")
                .isDefault(isFirst || dto.isDefault())
                .latitude(dto.getLatitude() != null ? dto.getLatitude() : 12.9716)
                .longitude(dto.getLongitude() != null ? dto.getLongitude() : 77.5946)
                .build();

        if (address.isDefault()) {
            existing.forEach(a -> {
                a.setDefault(false);
                addressRepository.save(a);
            });
        }

        address = addressRepository.save(address);
        return mapToAddressDto(address);
    }

    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized address access");
        }

        addressRepository.delete(address);
    }

    private CommerceDto.AddressDto mapToAddressDto(Address a) {
        return CommerceDto.AddressDto.builder()
                .id(a.getId())
                .name(a.getName())
                .mobile(a.getMobile())
                .street(a.getStreet())
                .locality(a.getLocality())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .landmark(a.getLandmark())
                .addressType(a.getAddressType())
                .isDefault(a.isDefault())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .build();
    }
}
