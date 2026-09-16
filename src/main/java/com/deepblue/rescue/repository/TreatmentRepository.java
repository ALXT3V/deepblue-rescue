package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    List<Treatment> findByAnimalIdOrderByPerformedAtAsc(Long animalId);

    @Query("""
        SELECT t
        FROM Treatment t
        WHERE t.performedAt BETWEEN :start AND :end
        ORDER BY t.performedAt ASC
        """)
    List<Treatment> findByPerformedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT t
        FROM Treatment t
        WHERE t.animal.rescueCase.rescueCenter.code = :centerCode
        """)
    List<Treatment> findByRescueCenterCode(@Param("centerCode") String centerCode);

    @Query("""
        SELECT t
        FROM Treatment t
        JOIN t.specialist s
        JOIN s.expertiseAreas e
        WHERE e.name = :expertiseName
        """)
    List<Treatment> findBySpecialistExpertiseName(@Param("expertiseName") String expertiseName);
}