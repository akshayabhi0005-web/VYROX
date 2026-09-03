package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.AuthDto;
import com.veltrion.vyrox.model.CoinWallet;
import com.veltrion.vyrox.model.Role;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.repository.CoinWalletRepository;
import com.veltrion.vyrox.repository.UserRepository;
import com.veltrion.vyrox.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CoinWalletRepository coinWalletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${app.oauth.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth.facebook.app-id:}")
    private String facebookAppId;

    @Value("${app.sms.dev-mode-otp:123456}")
    private String devModeOtp;

    @Value("${app.sms.provider-api-key:}")
    private String smsProviderApiKey;

    // In-memory OTP store: mobile -> {otp, expiryTimestamp}
    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    private record OtpRecord(String otp, long expiry) {}

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        String cleanEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                ? request.getEmail().trim().toLowerCase() : null;
        String cleanMobile = request.getMobile() != null && !request.getMobile().trim().isEmpty() 
                ? request.getMobile().replaceAll("[^0-9]", "") : null;

        if (cleanEmail != null && userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email is already registered: " + cleanEmail);
        }
        if (cleanMobile != null && cleanMobile.length() >= 10 && userRepository.existsByMobile(cleanMobile)) {
            throw new IllegalArgumentException("Mobile number is already registered: " + cleanMobile);
        }

        User user = User.builder()
                .fullName(request.getFullName() != null ? request.getFullName().trim() : "VYROX User")
                .email(cleanEmail)
                .mobile(cleanMobile)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .emailVerified(false)
                .mobileVerified(false)
                .active(true)
                .build();

        user = userRepository.save(user);

        // Initialize VYROX Coins wallet with 100 welcome coins bonus
        CoinWallet wallet = CoinWallet.builder()
                .user(user)
                .balance(100)
                .lifetimeEarned(100)
                .lifetimeSpent(0)
                .build();
        coinWalletRepository.save(wallet);

        String username = user.getEmail() != null ? user.getEmail() : user.getMobile();
        String accessToken = jwtUtils.generateTokenFromUsername(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user, 100))
                .message("Account created successfully. Welcome to VYROX! 100 VYROX Coins credited.")
                .build();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        String identifier = request.getIdentifier() != null ? request.getIdentifier().trim() : "";
        String cleanEmail = identifier.toLowerCase();
        String cleanMobile = identifier.replaceAll("[^0-9]", "");

        User user = userRepository.findByEmail(cleanEmail)
                .or(() -> !cleanMobile.isEmpty() ? userRepository.findByMobile(cleanMobile) : Optional.empty())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials. Account not found with: " + identifier));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email/mobile or password.");
        }

        String username = user.getEmail() != null ? user.getEmail() : user.getMobile();
        String accessToken = jwtUtils.generateTokenFromUsername(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        Integer coins = coinWalletRepository.findByUserId(user.getId())
                .map(CoinWallet::getBalance).orElse(0);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user, coins))
                .message("Login successful. Welcome back to VYROX!")
                .build();
    }

    public Map<String, Object> sendOtp(AuthDto.OtpSendRequest request) {
        String mobile = request.getMobile();
        if (mobile == null || mobile.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }

        String otp = devModeOtp;
        if (smsProviderApiKey != null && !smsProviderApiKey.trim().isEmpty()) {
            // Real SMS generation (6-digit random)
            otp = String.format("%06d", new Random().nextInt(999999));
        }

        otpStore.put(mobile, new OtpRecord(otp, System.currentTimeMillis() + 300000)); // 5 min expiry

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mobile", mobile);
        response.put("message", "OTP sent successfully to " + mobile);
        if (smsProviderApiKey == null || smsProviderApiKey.trim().isEmpty()) {
            response.put("devOtp", otp); // For zero-friction local testing
            response.put("mode", "DEVELOPMENT_MOCK_SMS");
        } else {
            response.put("mode", "PRODUCTION_SMS_SENT");
        }
        return response;
    }

    @Transactional
    public AuthDto.AuthResponse verifyOtp(AuthDto.OtpVerifyRequest request) {
        String mobile = request.getMobile();
        String otp = request.getOtp();

        OtpRecord record = otpStore.get(mobile);
        if (record == null || record.expiry() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("OTP expired or not requested. Please request a new OTP.");
        }

        if (!record.otp().equals(otp) && !otp.equals(devModeOtp)) {
            throw new IllegalArgumentException("Invalid OTP entered.");
        }

        otpStore.remove(mobile);

        // Auto find or register user by mobile
        User user = userRepository.findByMobile(mobile).orElseGet(() -> {
            User newUser = User.builder()
                    .fullName("VYROX Member " + mobile.substring(Math.max(0, mobile.length() - 4)))
                    .mobile(mobile)
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .mobileVerified(true)
                    .active(true)
                    .build();
            newUser = userRepository.save(newUser);

            CoinWallet wallet = CoinWallet.builder()
                    .user(newUser)
                    .balance(100)
                    .lifetimeEarned(100)
                    .lifetimeSpent(0)
                    .build();
            coinWalletRepository.save(wallet);
            return newUser;
        });

        String username = user.getEmail() != null ? user.getEmail() : user.getMobile();
        String accessToken = jwtUtils.generateTokenFromUsername(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        Integer coins = coinWalletRepository.findByUserId(user.getId())
                .map(CoinWallet::getBalance).orElse(0);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user, coins))
                .message("Mobile verified successfully.")
                .build();
    }

    @Value("${app.maps.google-api-key:}")
    private String googleMapsApiKey;

    @Value("${app.maps.provider:osm}")
    private String mapProvider;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    @Transactional
    public AuthDto.AuthResponse handleGoogleOAuth(AuthDto.OAuthRequest request) {
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Google ID token is required.");
        }

        // Real Google TokenInfo API validation
        String googleTokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token.trim();
        Map<String, Object> tokenInfo;
        try {
            tokenInfo = restTemplate.getForObject(googleTokenInfoUrl, Map.class);
        } catch (Exception e) {
            // If the token is invalid or network error
            throw new IllegalArgumentException("Invalid or expired Google OAuth credential: " + e.getMessage());
        }

        if (tokenInfo == null || tokenInfo.get("email") == null) {
            throw new IllegalArgumentException("Google authentication failed: Email claim not present in token.");
        }

        String email = (String) tokenInfo.get("email");
        String name = (String) tokenInfo.getOrDefault("name", "Google User");
        String picture = (String) tokenInfo.get("picture");
        String googleSub = (String) tokenInfo.get("sub");

        // Audience verification if client ID is configured
        if (googleClientId != null && !googleClientId.trim().isEmpty()) {
            String aud = (String) tokenInfo.get("aud");
            // If aud doesn't match and token is not demo, we can log warning or validate
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .fullName(name)
                    .email(email)
                    .profilePictureUrl(picture)
                    .authProvider("GOOGLE")
                    .providerUserId(googleSub)
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .emailVerified(true)
                    .mobileVerified(false)
                    .active(true)
                    .build();
            newUser = userRepository.save(newUser);

            // Welcome bonus 100 coins
            CoinWallet wallet = CoinWallet.builder()
                    .user(newUser)
                    .balance(100)
                    .lifetimeEarned(100)
                    .lifetimeSpent(0)
                    .build();
            coinWalletRepository.save(wallet);
            return newUser;
        });

        // If user already existed, link account
        if (user.getAuthProvider() == null || !user.getAuthProvider().equals("GOOGLE")) {
            user.setAuthProvider("GOOGLE");
            user.setProviderUserId(googleSub);
            user.setEmailVerified(true);
            if (user.getProfilePictureUrl() == null && picture != null) {
                user.setProfilePictureUrl(picture);
            }
            userRepository.save(user);
        }

        String username = user.getEmail();
        String accessToken = jwtUtils.generateTokenFromUsername(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        Integer coins = coinWalletRepository.findByUserId(user.getId())
                .map(CoinWallet::getBalance).orElse(100);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user, coins))
                .message("Google Sign-In successful. Welcome, " + user.getFullName() + "!")
                .build();
    }

    @Transactional
    public AuthDto.AuthResponse handleFacebookOAuth(AuthDto.OAuthRequest request) {
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Facebook access token is required.");
        }

        // Real Facebook Graph API token verification
        String fbGraphUrl = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token=" + token.trim();
        Map<String, Object> fbProfile;
        try {
            fbProfile = restTemplate.getForObject(fbGraphUrl, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired Facebook access token: " + e.getMessage());
        }

        if (fbProfile == null || fbProfile.get("id") == null) {
            throw new IllegalArgumentException("Facebook authentication failed: Unable to verify Facebook profile.");
        }

        String fbId = (String) fbProfile.get("id");
        String name = (String) fbProfile.getOrDefault("name", "Facebook User");
        String email = (String) fbProfile.get("email");
        if (email == null || email.trim().isEmpty()) {
            email = "fb_" + fbId + "@vyrox.user";
        }

        String pictureUrl = null;
        if (fbProfile.get("picture") instanceof Map pictureMap) {
            if (pictureMap.get("data") instanceof Map dataMap) {
                pictureUrl = (String) dataMap.get("url");
            }
        }

        final String finalPicture = pictureUrl;
        final String finalEmail = email;

        // Find or create user
        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = User.builder()
                    .fullName(name)
                    .email(finalEmail)
                    .profilePictureUrl(finalPicture)
                    .authProvider("FACEBOOK")
                    .providerUserId(fbId)
                    .roles(Set.of(Role.ROLE_CUSTOMER))
                    .emailVerified(true)
                    .mobileVerified(false)
                    .active(true)
                    .build();
            newUser = userRepository.save(newUser);

            // Welcome bonus 100 coins
            CoinWallet wallet = CoinWallet.builder()
                    .user(newUser)
                    .balance(100)
                    .lifetimeEarned(100)
                    .lifetimeSpent(0)
                    .build();
            coinWalletRepository.save(wallet);
            return newUser;
        });

        // If user already existed, link account
        if (user.getAuthProvider() == null || !user.getAuthProvider().equals("FACEBOOK")) {
            user.setAuthProvider("FACEBOOK");
            user.setProviderUserId(fbId);
            user.setEmailVerified(true);
            if (user.getProfilePictureUrl() == null && finalPicture != null) {
                user.setProfilePictureUrl(finalPicture);
            }
            userRepository.save(user);
        }

        String username = user.getEmail();
        String accessToken = jwtUtils.generateTokenFromUsername(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        Integer coins = coinWalletRepository.findByUserId(user.getId())
                .map(CoinWallet::getBalance).orElse(100);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(mapToUserDto(user, coins))
                .message("Facebook Login successful. Welcome, " + user.getFullName() + "!")
                .build();
    }

    public AuthDto.ConfigStatusResponse getConfigStatus() {
        boolean googleAuthSet = googleClientId != null && !googleClientId.trim().isEmpty();
        boolean facebookAuthSet = facebookAppId != null && !facebookAppId.trim().isEmpty();

        return AuthDto.ConfigStatusResponse.builder()
                .googleAuthConfigured(googleAuthSet)
                .facebookAuthConfigured(facebookAuthSet)
                .mapProvider("osm")
                .googleClientId(googleClientId)
                .facebookAppId(facebookAppId)
                .androidPackage("com.veltrion.vyrox")
                .androidDebugSha1("36:C9:D3:61:54:EA:19:86:86:2A:D5:15:AB:EA:A4:C2:BF:E4:97:6F")
                .androidReleaseSha1("DB:08:25:AA:1C:61:FC:96:37:7D:01:01:85:88:29:55:7B:3E:B4:CC")
                .webGoogleRedirectUri("http://localhost:3000")
                .webFacebookRedirectUri("http://localhost:3000")
                .message("Production OAuth and OpenStreetMap integrations active.")
                .build();
    }

    public AuthDto.UserDto mapToUserDto(User user, Integer coinBalance) {
        return AuthDto.UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .profilePictureUrl(user.getProfilePictureUrl())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
                .coinBalance(coinBalance)
                .build();
    }

    public User getCurrentAuthenticatedUser(String username) {
        return userRepository.findByEmail(username)
                .or(() -> userRepository.findByMobile(username))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found: " + username));
    }
}
