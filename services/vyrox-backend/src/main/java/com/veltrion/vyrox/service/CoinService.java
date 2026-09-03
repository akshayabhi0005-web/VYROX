package com.veltrion.vyrox.service;

import com.veltrion.vyrox.dto.CommerceDto;
import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.CoinTransactionRepository;
import com.veltrion.vyrox.repository.CoinWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinWalletRepository walletRepository;
    private final CoinTransactionRepository transactionRepository;

    @Transactional
    public CoinWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId()).orElseGet(() -> {
            CoinWallet wallet = CoinWallet.builder()
                    .user(user)
                    .balance(100)
                    .lifetimeEarned(100)
                    .lifetimeSpent(0)
                    .build();
            wallet = walletRepository.save(wallet);

            CoinTransaction tx = CoinTransaction.builder()
                    .wallet(wallet)
                    .type(CoinTransactionType.EARNED_REWARD)
                    .amount(100)
                    .description("Welcome Bonus on joining VYROX")
                    .build();
            transactionRepository.save(tx);
            return wallet;
        });
    }

    @Transactional(readOnly = true)
    public CommerceDto.CoinWalletDto getWalletDto(User user) {
        CoinWallet wallet = getOrCreateWallet(user);
        List<CoinTransaction> txs = transactionRepository.findByWalletIdOrderByTimestampDesc(wallet.getId());

        List<CommerceDto.CoinTransactionDto> txDtos = txs.stream().map(t -> CommerceDto.CoinTransactionDto.builder()
                .id(t.getId())
                .type(t.getType().name())
                .amount(t.getAmount())
                .description(t.getDescription())
                .referenceId(t.getReferenceId())
                .timestamp(t.getTimestamp())
                .build()).collect(Collectors.toList());

        return CommerceDto.CoinWalletDto.builder()
                .balance(wallet.getBalance())
                .lifetimeEarned(wallet.getLifetimeEarned())
                .lifetimeSpent(wallet.getLifetimeSpent())
                .recentTransactions(txDtos)
                .build();
    }

    @Transactional
    public void creditCoins(User user, int amount, CoinTransactionType type, String description, String referenceId) {
        CoinWallet wallet = getOrCreateWallet(user);
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setLifetimeEarned(wallet.getLifetimeEarned() + amount);
        walletRepository.save(wallet);

        CoinTransaction tx = CoinTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .description(description)
                .referenceId(referenceId)
                .build();
        transactionRepository.save(tx);
    }

    @Transactional
    public boolean debitCoins(User user, int amount, String description, String referenceId) {
        CoinWallet wallet = getOrCreateWallet(user);
        if (wallet.getBalance() < amount) {
            return false;
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setLifetimeSpent(wallet.getLifetimeSpent() + amount);
        walletRepository.save(wallet);

        CoinTransaction tx = CoinTransaction.builder()
                .wallet(wallet)
                .type(CoinTransactionType.REDEEMED_PURCHASE)
                .amount(-amount)
                .description(description)
                .referenceId(referenceId)
                .build();
        transactionRepository.save(tx);
        return true;
    }

    @Transactional
    public CommerceDto.CoinWalletDto spinAndWin(User user) {
        // Spin and win reward: 20 to 150 coins
        int[] rewards = {25, 50, 75, 100, 150};
        int reward = rewards[new Random().nextInt(rewards.length)];

        creditCoins(user, reward, CoinTransactionType.EARNED_SPIN_WIN, "Won from Daily Spin & Win!", "SPIN-" + System.currentTimeMillis());
        return getWalletDto(user);
    }
}
