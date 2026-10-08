package com.deepblue.rescue.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_code", nullable = false, unique = true, length = 50)
    private String caseCode;

  @Column(name = "rescue_date", nullable = true)
private LocalDate rescueDate;

@Column(name = "rescue_location", nullable = true, length = 300)
private String rescueLocation;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = true, length = 30)
private RescueStatus status;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "rescue_center_id", nullable = true)
private RescueCenter rescueCenter;

    @OneToOne(
        mappedBy = "rescueCase",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private Animal animal;
    
    public void assignAnimal(Animal animal) {
        if (animal != null) {
            animal.setRescueCase(this);
        } else if (this.animal != null) {
            this.animal.setRescueCase(null);
        }
        this.animal = animal;
    }

    public RescueCase() {
    }

    public RescueCase(String caseCode, LocalDate rescueDate, String rescueLocation, RescueStatus status) {
        this.caseCode = caseCode;
        this.rescueDate = rescueDate;
        this.rescueLocation = rescueLocation;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseCode() {
        return caseCode;
    }

    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }

    public LocalDate getRescueDate() {
        return rescueDate;
    }

    public void setRescueDate(LocalDate rescueDate) {
        this.rescueDate = rescueDate;
    }

    public String getRescueLocation() {
        return rescueLocation;
    }

    public void setRescueLocation(String rescueLocation) {
        this.rescueLocation = rescueLocation;
    }

    public RescueStatus getStatus() {
        return status;
    }

    public void setStatus(RescueStatus status) {
        this.status = status;
    }

    public RescueCenter getRescueCenter() {
        return rescueCenter;
    }

    public void setRescueCenter(RescueCenter rescueCenter) {
        this.rescueCenter = rescueCenter;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        assignAnimal(animal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RescueCase that = (RescueCase) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}