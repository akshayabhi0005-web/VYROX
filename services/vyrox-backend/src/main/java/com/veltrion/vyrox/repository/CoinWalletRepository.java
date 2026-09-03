package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.CoinWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CoinWalletRepository extends JpaRepository<CoinWallet, Long> {
    Optional<CoinWallet> findByUserId(Long userId);
}
