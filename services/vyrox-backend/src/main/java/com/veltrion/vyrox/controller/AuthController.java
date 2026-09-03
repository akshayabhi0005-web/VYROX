package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.AuthDto;
import com.veltrion.vyrox.security.JwtUtils;
import com.veltrion.vyrox.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication, OTP, OAuth, and password recovery")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "Register new user account")
    public ResponseEntity<AuthDto.AuthResponse> register(@RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with Email or Mobile and Password")
    public ResponseEntity<AuthDto.AuthResponse> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to Mobile number")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody AuthDto.OtpSendRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and login or auto-register")
    public ResponseEntity<AuthDto.AuthResponse> verifyOtp(@RequestBody AuthDto.OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/oauth/google")
    @Operation(summary = "Google OAuth 2.0 Sign In")
    public ResponseEntity<AuthDto.AuthResponse> googleOAuth(@RequestBody AuthDto.OAuthRequest request) {
        return ResponseEntity.ok(authService.handleGoogleOAuth(request));
    }

    @PostMapping("/oauth/facebook")
    @Operation(summary = "Facebook OAuth Sign In")
    public ResponseEntity<AuthDto.AuthResponse> facebookOAuth(@RequestBody AuthDto.OAuthRequest request) {
        return ResponseEntity.ok(authService.handleFacebookOAuth(request));
    }

    @GetMapping("/config-status")
    @Operation(summary = "Get OAuth and Maps integration status")
    public ResponseEntity<AuthDto.ConfigStatusResponse> getConfigStatus() {
        return ResponseEntity.ok(authService.getConfigStatus());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        if (jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            String newAccessToken = jwtUtils.generateTokenFromUsername(username);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "tokenType", "Bearer", "expiresIn", 86400L));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired refresh token"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
