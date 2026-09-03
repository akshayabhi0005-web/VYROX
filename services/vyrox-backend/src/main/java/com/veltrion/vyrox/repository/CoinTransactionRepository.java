package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.CoinTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransaction, Long> {
    List<CoinTransaction> findByWalletIdOrderByTimestampDesc(Long walletId);
}
