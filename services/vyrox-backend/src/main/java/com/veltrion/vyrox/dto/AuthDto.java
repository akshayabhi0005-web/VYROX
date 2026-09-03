package com.veltrion.vyrox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String identifier; // email or mobile
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String fullName;
        private String email;
        private String mobile;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpSendRequest {
        private String mobile;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpVerifyRequest {
        private String mobile;
        private String otp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        private String identifier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        private String identifier;
        private String otp;
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuthRequest {
        private String token; // OAuth token
        private String provider; // "google" or "facebook"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private UserDto user;
        private String message;
        private boolean configurationRequired; // If OAuth/SMS is unconfigured
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private Long id;
        private String fullName;
        private String email;
        private String mobile;
        private String profilePictureUrl;
        private List<String> roles;
        private Integer coinBalance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigStatusResponse {
        private boolean googleAuthConfigured;
        private boolean facebookAuthConfigured;
        private String mapProvider; // "osm"
        private String googleClientId;
        private String facebookAppId;
        private String androidPackage;
        private String androidDebugSha1;
        private String androidReleaseSha1;
        private String webGoogleRedirectUri;
        private String webFacebookRedirectUri;
        private String message;
    }
}
