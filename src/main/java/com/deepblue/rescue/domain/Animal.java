package com.deepblue.rescue.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "animal_code", nullable = false, unique = true, length = 50)
    private String animalCode;

    @Column(name = "common_name", nullable = false, length = 150)
    private String commonName;

    @Column(name = "scientific_name", nullable = false, length = 200)
    private String scientificName;

    @Column(name = "sex", length = 10)
    private String sex;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "rescue_case_id",
        nullable = false,
        unique = true
    )
    private RescueCase rescueCase;

    @OneToMany(mappedBy = "animal")
private List<Treatment> treatments;

    @OneToOne(
        mappedBy = "animal",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch=FetchType.LAZY
    )  
    private MedicalRecord medicalRecord;
    @Column(name = "tracking_device_code", length = 50, unique = true)
    private String trackingDeviceCode;

    public String getTrackingDeviceCode() {
        return trackingDeviceCode;
    }

    public void setTrackingDeviceCode(String trackingDeviceCode) {
        this.trackingDeviceCode = trackingDeviceCode;
    }
    
    public void assignMedicalRecord(MedicalRecord medicalRecord){
        if (medicalRecord ==null){
            if (this.medicalRecord != null){
                this.medicalRecord.setAnimal(null);
            }
        }else{
            medicalRecord.setAnimal(this);
        }
        this.medicalRecord = medicalRecord;
    }


    public Animal() {
    }

    public Animal(String animalCode, String commonName, String scientificName, String sex) {
        this.animalCode = animalCode;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.sex = sex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnimalCode() {
        return animalCode;
    }

    public void setAnimalCode(String animalCode) {
        this.animalCode = animalCode;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public RescueCase getRescueCase() {
        return rescueCase;
    }

    public void setRescueCase(RescueCase rescueCase) {
        this.rescueCase = rescueCase;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Animal animal = (Animal) o;
        return Objects.equals(id, animal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}