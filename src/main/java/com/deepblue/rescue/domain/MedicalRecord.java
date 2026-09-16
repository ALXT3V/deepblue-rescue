package com.deepblue.rescue.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "initial_weight", precision = 6, scale = 2)
    private BigDecimal initialWeight;

    @Column(name = "initial_condition", nullable = false, columnDefinition = "TEXT")
    private String initialCondition;

    @Column(name = "injuries", columnDefinition = "TEXT")
    private String injuries;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "animal_id",
        nullable = false,
        unique = true
    )
    private Animal animal;

    public MedicalRecord() {
    }

    public MedicalRecord(BigDecimal initialWeight, String initialCondition, String injuries, String observations) {
        this.initialWeight = initialWeight;
        this.initialCondition = initialCondition;
        this.injuries = injuries;
        this.observations = observations;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getInitialWeight() { return initialWeight; }
    public void setInitialWeight(BigDecimal initialWeight) { this.initialWeight = initialWeight; }

    public String getInitialCondition() { return initialCondition; }
    public void setInitialCondition(String initialCondition) { this.initialCondition = initialCondition; }

    public String getInjuries() { return injuries; }
    public void setInjuries(String injuries) { this.injuries = injuries; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}