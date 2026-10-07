package com.deepblue.rescue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.AnimalService;


@Service
@Transactional(readOnly = true)
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper mapper;

    public AnimalServiceImpl(AnimalRepository animalRepository, AnimalMapper mapper) {
        this.animalRepository = animalRepository;
        this.mapper = mapper;
    }

    @Override
    public AnimalResponse findByCode(String animalCode) {
        return animalRepository.findByAnimalCode(animalCode)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + animalCode));
    }

    @Override
    public List<AnimalResponse> findAnimalsInRehabilitation() {
        return animalRepository.findByRescueCaseStatus(RescueStatus.IN_REHABILITATION)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public boolean canReceiveTreatment(String animalCode) {
        AnimalResponse animal = findByCode(animalCode);
        RescueStatus status = animal.rescueStatus();
        return status == RescueStatus.UNDER_EVALUATION || status == RescueStatus.IN_REHABILITATION;
    }
}