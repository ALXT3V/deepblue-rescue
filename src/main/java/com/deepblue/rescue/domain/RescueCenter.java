package com.deepblue.rescue.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Entity
    @Table(name="rescue_centers")
    
public class RescueCenter {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(name="code", nullable = false, unique = true, length = 20 )
private String code;

@Column(name="name", nullable = false, length = 100)
private String name;

@Column(name="city", nullable = false, length =100)
private String city;

@OneToMany(mappedBy="rescueCenter", cascade = CascadeType.ALL, orphanRemoval = true)
private List<RescueCase> rescueCases = new ArrayList<>();

public RescueCenter(){

}
public RescueCenter(String code, String name, String city){
    this.code=code;
    this.name=name;
    this.city=city;

}
public void addcase(RescueCase rescueCase){
    rescueCases.add(rescueCase);
    rescueCase.setRescueCenter(this);

}
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<RescueCase> getRescueCases() {
        return rescueCases;
    }

    public void setRescueCases(List<RescueCase> rescueCases) {
        this.rescueCases = rescueCases;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RescueCenter that = (RescueCenter) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
}
