package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface RescueCaseRepository extends JpaRepository<RescueCase, Long> {
    Optional<RescueCase> findByCaseCode(String caseCode);
    List<RescueCase> findByStatusOrderByRescueDateAsc(RescueStatus status);
    List<RescueCase> findByRescueCenterCode(String code);
    List<RescueCase> findByRescueDateAfterOrderByRescueDateDesc(LocalDateTime date);

}