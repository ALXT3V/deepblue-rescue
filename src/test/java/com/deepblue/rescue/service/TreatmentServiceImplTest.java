package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.impl.TreatmentServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    @Test
    void shouldRegisterTreatmentSuccessfully() {

        RescueCase rescueCase = new RescueCase();
        rescueCase.setStatus(RescueStatus.IN_REHABILITATION);
        rescueCase.setRescueDate(LocalDate.now().minusDays(2));

        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setRescueCase(rescueCase);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setActive(true);

        LocalDateTime performedAt = LocalDateTime.now();

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                performedAt,
                TreatmentType.WOUND_CARE,
                "Wound care treatment"
        );

        Treatment savedTreatment = new Treatment();

        TreatmentResponse response = new TreatmentResponse(
                1L,
                "AN-001",
                "SPEC-001",
                performedAt,
                TreatmentType.WOUND_CARE,
                "Wound care treatment"
        );

        when(
            animalRepository.findByAnimalCode("AN-001")
        ).thenReturn(
            Optional.of(animal)
        );

        when(
            specialistRepository.findByProfessionalCode("SPEC-001")
        ).thenReturn(
            Optional.of(specialist)
        );

        when(
            treatmentRepository.save(any(Treatment.class))
        ).thenReturn(savedTreatment);

        when(
            mapper.toResponse(savedTreatment)
        ).thenReturn(response);

        TreatmentResponse result =
                service.register(request);

        assertThat(result)
                .isEqualTo(response);

        verify(animalRepository)
                .findByAnimalCode("AN-001");

        verify(specialistRepository)
                .findByProfessionalCode("SPEC-001");

        verify(treatmentRepository)
                .save(any(Treatment.class));

        verify(mapper)
                .toResponse(savedTreatment);
    }

    @Test
    void shouldThrowExceptionWhenSpecialistIsInactive() {

        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setActive(false);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                LocalDateTime.now(),
                TreatmentType.WOUND_CARE,
                "Wound care treatment"
        );

        when(
            animalRepository.findByAnimalCode("AN-001")
        ).thenReturn(
            Optional.of(animal)
        );

        when(
            specialistRepository.findByProfessionalCode("SPEC-001")
        ).thenReturn(
            Optional.of(specialist)
        );

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(animalRepository)
                .findByAnimalCode("AN-001");

        verify(specialistRepository)
                .findByProfessionalCode("SPEC-001");

        verify(treatmentRepository, never())
                .save(any());

        verifyNoInteractions(mapper);
    }

    @Test
    void shouldThrowExceptionWhenRescueCaseIsReleased() {

        RescueCase rescueCase = new RescueCase();
        rescueCase.setStatus(RescueStatus.RELEASED);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setRescueCase(rescueCase);

        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setActive(true);

        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001",
                "SPEC-001",
                LocalDateTime.now(),
                TreatmentType.OBSERVATION,
                "Observation treatment"
        );

        when(
            animalRepository.findByAnimalCode("AN-001")
        ).thenReturn(
            Optional.of(animal)
        );

        when(
            specialistRepository.findByProfessionalCode("SPEC-001")
        ).thenReturn(
            Optional.of(specialist)
        );

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(animalRepository)
                .findByAnimalCode("AN-001");

        verify(specialistRepository)
                .findByProfessionalCode("SPEC-001");

        verify(treatmentRepository, never())
                .save(any());

        verifyNoInteractions(mapper);
    }
}