package com.veltrion.vyrox.repository;

import com.veltrion.vyrox.model.TrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingLogRepository extends JpaRepository<TrackingLog, Long> {
    List<TrackingLog> findByOrderIdOrderByTimestampAsc(Long orderId);
}
