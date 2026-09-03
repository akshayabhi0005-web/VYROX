package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile and address management")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/addresses")
    @Operation(summary = "Get user delivery addresses")
    public ResponseEntity<List<CommerceDto.AddressDto>> getAddresses(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(userService.getUserAddresses(user));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new delivery address")
    public ResponseEntity<CommerceDto.AddressDto> addAddress(
            Authentication authentication,
            @RequestBody CommerceDto.AddressDto addressDto
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(userService.addAddress(user, addressDto));
    }

    @DeleteMapping("/addresses/{addressId}")
    @Operation(summary = "Delete delivery address")
    public ResponseEntity<?> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId
    ) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        userService.deleteAddress(user, addressId);
        return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
    }
}
