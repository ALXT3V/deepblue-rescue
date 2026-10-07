package com.deepblue.rescue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.AnimalMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.service.impl.AnimalServiceImpl;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    private AnimalServiceImpl service;

    @Test
    void shouldFindAnimalByCodeSuccessfully() {
        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");

        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Green Sea Turtle", "Chelonia mydas", "FEMALE", "RES-001", RescueStatus.IN_REHABILITATION
        );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(mapper.toResponse(animal)).thenReturn(response);

        AnimalResponse result = service.findByCode("AN-001");

        assertThat(result).isEqualTo(response);
        verify(animalRepository).findByAnimalCode("AN-001");
        verify(mapper).toResponse(animal);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenAnimalNotFound() {
        when(animalRepository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("AN-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Animal not found: AN-999");

        verify(animalRepository).findByAnimalCode("AN-999");
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnTrueWhenAnimalCanReceiveTreatment() {
        Animal animal = new Animal();
        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Turtle", "Chelonia", "FEMALE", "RES-001", RescueStatus.IN_REHABILITATION
        );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(mapper.toResponse(animal)).thenReturn(response);

        boolean eligible = service.canReceiveTreatment("AN-001");

        assertThat(eligible).isTrue();
        verify(animalRepository).findByAnimalCode("AN-001");
    }

    @Test
    void shouldReturnFalseWhenAnimalCannotReceiveTreatment() {
        Animal animal = new Animal();
        AnimalResponse response = new AnimalResponse(
                1L, "AN-001", "Turtle", "Chelonia", "FEMALE", "RES-001", RescueStatus.RELEASED
        );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(mapper.toResponse(animal)).thenReturn(response);

        boolean eligible = service.canReceiveTreatment("AN-001");

        assertThat(eligible).isFalse();
        verify(animalRepository).findByAnimalCode("AN-001");
    }
}