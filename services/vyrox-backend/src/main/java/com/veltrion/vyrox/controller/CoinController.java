package com.veltrion.vyrox.controller;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.User;
import com.veltrion.vyrox.service.AuthService;
import com.veltrion.vyrox.service.CoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coins")
@RequiredArgsConstructor
@Tag(name = "VYROX Coins", description = "Rewards, Coin balance, and Spin & Win")
public class CoinController {

    private final CoinService coinService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get user coin wallet balance and transaction history")
    public ResponseEntity<CommerceDto.CoinWalletDto> getWallet(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(coinService.getWalletDto(user));
    }

    @PostMapping("/spin-and-win")
    @Operation(summary = "Play daily Spin & Win to earn VYROX coins (Server validated)")
    public ResponseEntity<CommerceDto.CoinWalletDto> spinAndWin(Authentication authentication) {
        User user = authService.getCurrentAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(coinService.spinAndWin(user));
    }
}
