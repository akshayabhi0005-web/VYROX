package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.Darkstore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DarkstoreRepository extends JpaRepository<Darkstore, Long> {
    List<Darkstore> findByActiveTrue();
    Optional<Darkstore> findByCode(String code);
}
