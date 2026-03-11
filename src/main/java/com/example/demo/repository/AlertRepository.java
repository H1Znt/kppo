package com.example.demo.repository;

import com.example.demo.model.Alert;
import com.example.demo.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
  List<Alert> findByStatus(StatusType status);
  
  List<Alert> findByStatusOrderByTimestampDesc(StatusType status);
  
  @Query("SELECT a FROM Alert a WHERE a.timestamp BETWEEN :start AND :end")
  List<Alert> findAlertsBetweenDates(
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end
  );
  
  List<Alert> findBySensorId(Long sensorId);
}
