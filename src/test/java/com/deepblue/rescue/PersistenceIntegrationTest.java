package com;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.Expertise;
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.ExpertiseRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.RescueCenterRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.transaction.Transactional;
import com.deepblue.rescue.domain.TreatmentType;

@Testcontainers
@SpringBootTest
@Transactional
public class PersistenceIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");
                    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private RescueCaseRepository rescueCaseRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndDependenciesInjected() {
        assertThat(rescueCenterRepository).isNotNull();
        assertThat(rescueCaseRepository).isNotNull();
        assertThat(animalRepository).isNotNull();
        assertThat(specialistRepository).isNotNull();
        assertThat(expertiseRepository).isNotNull();
        assertThat(treatmentRepository).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
    }
    @Test
    void flywayMigrationsExecutedSuccessfully() {
        String sql = "SELECT version FROM flyway_schema_history WHERE success = true";
        
        List<String> executedVersions = jdbcTemplate.queryForList(sql, String.class);

        assertThat(executedVersions).contains("1", "2");
    }
    @Test
    void testInheritedRepositoryMethods() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean Center");
        center.setCity("Santa Marta");

        RescueCenter saved = rescueCenterRepository.save(center);

        assertThat(saved.getId()).isNotNull();

        Optional<RescueCenter> found = rescueCenterRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("DeepBlue Caribbean Center");

        boolean exists = rescueCenterRepository.existsById(saved.getId());
        assertThat(exists).isTrue();

        long count = rescueCenterRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }
    @Test
    void shouldPersistAndRetrieveOneToManyRelationship() {
        RescueCenter center = new RescueCenter();
        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean Center");
        center.setCity("Santa Marta");

        RescueCenter savedCenter = rescueCenterRepository.save(center);

        RescueCase case1 = new RescueCase();
        case1.setCaseCode("CASE-001");
        case1.setRescueCenter(savedCenter);

        RescueCase case2 = new RescueCase();
        case2.setCaseCode("CASE-002");
        case2.setRescueCenter(savedCenter);

        RescueCase savedCase1 = rescueCaseRepository.save(case1);
        RescueCase savedCase2 = rescueCaseRepository.save(case2);

        assertThat(savedCase1.getRescueCenter().getId())
                .isEqualTo(savedCase2.getRescueCenter().getId());
        assertThat(savedCase1.getRescueCenter().getCode())
                .isEqualTo("DB-CAR");
        assertThat(savedCase2.getRescueCenter().getCode())
                .isEqualTo("DB-CAR");
    }
   @Test
    void shouldPersistAndRetrieveOneToOneRelationshipBetweenRescueCaseAndAnimal() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-001");

        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");

        rescueCase.setAnimal(animal);
        animal.setRescueCase(rescueCase);

        RescueCase savedCase = rescueCaseRepository.save(rescueCase);

        assertThat(savedCase.getAnimal()).isNotNull();
        assertThat(savedCase.getAnimal().getAnimalCode()).isEqualTo("AN-2026-001");

        Optional<Animal> foundAnimal = animalRepository.findByAnimalCode("AN-2026-001");
        assertThat(foundAnimal).isPresent();
        assertThat(foundAnimal.get().getRescueCase()).isNotNull();
        assertThat(foundAnimal.get().getRescueCase().getCaseCode()).isEqualTo("RES-2026-001");
    }
    @Test
    void shouldPersistAnimalAndMedicalRecordWithCascade() {
        Animal animal = new Animal();
        animal.setAnimalCode("AN-2026-002");

        MedicalRecord record = new MedicalRecord();
        record.setInitialWeight(new BigDecimal("28.40"));
        record.setInitialCondition("STABLE");
        record.setInjuries("Left front flipper injury");

        animal.setMedicalRecord(record);
        record.setAnimal(animal);

        Animal savedAnimal = animalRepository.save(animal);

        assertThat(savedAnimal.getId()).isNotNull();
        assertThat(savedAnimal.getMedicalRecord()).isNotNull();
        assertThat(savedAnimal.getMedicalRecord().getId()).isNotNull();
        assertThat(savedAnimal.getMedicalRecord().getInitialWeight()).isEqualTo(28.40);
        assertThat(savedAnimal.getMedicalRecord().getInitialCondition()).isEqualTo("STABLE");
    }
    @Test
    void shouldPersistAndRetrieveManyToManyRelationshipBetweenSpecialistAndExpertise() {
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma")
                .orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation")
                .orElseThrow();

        Specialist specialist = new Specialist();
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setProfessionalCode("SPEC-EV-01");
        specialist.setEmail("elena.vargas@deepblue.org");
        specialist.setActive(true);

        specialist.getExpertiseAreas().add(trauma);
        specialist.getExpertiseAreas().add(rehabilitation);

        Specialist savedSpecialist = specialistRepository.save(specialist);

        assertThat(savedSpecialist.getId()).isNotNull();

        Specialist found = specialistRepository.findById(savedSpecialist.getId())
                .orElseThrow();

        assertThat(found.getExpertiseAreas()).hasSize(2);
        assertThat(found.getExpertiseAreas())
                .extracting(Expertise::getName)
                .containsExactlyInAnyOrder("Trauma", "Rehabilitation");
    }
    @Test
    void testQueryMethodByStatus() {
        RescueCase case1 = new RescueCase();
        case1.setCaseCode("RES-001");
        case1.setStatus(RescueStatus.IN_REHABILITATION);

        RescueCase case2 = new RescueCase();
        case2.setCaseCode("RES-002");
        case2.setStatus(RescueStatus.READY_FOR_RELEASE);

        RescueCase case3 = new RescueCase();
        case3.setCaseCode("RES-003");
        case3.setStatus(RescueStatus.IN_REHABILITATION);

        rescueCaseRepository.save(case1);
        rescueCaseRepository.save(case2);
        rescueCaseRepository.save(case3);

        List<RescueCase> result = rescueCaseRepository.findByStatus(RescueStatus.IN_REHABILITATION);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RescueCase::getCaseCode)
                .containsExactlyInAnyOrder("RES-001", "RES-003");
    }
    @Test
    void testQueryMethodNavigatingRelationships() {
        RescueCenter dbCar = new RescueCenter();
        dbCar.setCode("DB-CAR");
        dbCar.setName("Caribbean Center");
        dbCar.setCity("Santa Marta");

        RescueCenter dbPac = new RescueCenter();
        dbPac.setCode("DB-PAC");
        dbPac.setName("Pacific Center");
        dbPac.setCity("Cali");

        RescueCenter savedCar = rescueCenterRepository.save(dbCar);
        RescueCenter savedPac = rescueCenterRepository.save(dbPac);

        RescueCase caseCar = new RescueCase();
        caseCar.setCaseCode("RES-CAR-01");
        caseCar.setRescueCenter(savedCar);

        RescueCase casePac = new RescueCase();
        casePac.setCaseCode("RES-PAC-01");
        casePac.setRescueCenter(savedPac);

        RescueCase savedCaseCar = rescueCaseRepository.save(caseCar);
        RescueCase savedCasePac = rescueCaseRepository.save(casePac);

        Animal animal1 = new Animal();
        animal1.setAnimalCode("AN-CAR-01");
        animal1.setCommonName("Dolphin");
        animal1.setScientificName("Delphinidae");
        animal1.setRescueCase(savedCaseCar);

        Animal animal2 = new Animal();
        animal2.setAnimalCode("AN-PAC-01");
        animal2.setCommonName("Whale");
        animal2.setScientificName("Balaenopteridae");
        animal2.setRescueCase(savedCasePac);

        animalRepository.save(animal1);
        animalRepository.save(animal2);

        List<Animal> carAnimals = animalRepository.findByRescueCaseRescueCenterCode("DB-CAR");

        assertThat(carAnimals).hasSize(1);
        assertThat(carAnimals.get(0).getAnimalCode()).isEqualTo("AN-CAR-01");
    }
    @Test
    void testSpecialistsByExpertise() {
        Expertise trauma = expertiseRepository.findByNameIgnoreCase("Trauma").orElseThrow();
        Expertise rehabilitation = expertiseRepository.findByNameIgnoreCase("Rehabilitation").orElseThrow();
        Expertise marineMammals = expertiseRepository.findByNameIgnoreCase("Marine Mammals").orElseThrow();
        Expertise marineBirds = expertiseRepository.findByNameIgnoreCase("Marine Birds").orElseThrow();

        Specialist elena = new Specialist();
        elena.setFirstName("Elena");
        elena.setLastName("Vargas");
        elena.setProfessionalCode("SPEC-EV-02");
        elena.setEmail("elena.vargas2@deepblue.org");
        elena.setActive(true);
        elena.getExpertiseAreas().add(trauma);
        elena.getExpertiseAreas().add(rehabilitation);

        Specialist mateo = new Specialist();
        mateo.setFirstName("Mateo");
        mateo.setLastName("Gomez");
        mateo.setProfessionalCode("SPEC-MG-01");
        mateo.setEmail("mateo.gomez@deepblue.org");
        mateo.setActive(true);
        mateo.getExpertiseAreas().add(marineMammals);
        mateo.getExpertiseAreas().add(rehabilitation);

        Specialist sofia = new Specialist();
        sofia.setFirstName("Sofia");
        sofia.setLastName("Lopez");
        sofia.setProfessionalCode("SPEC-SL-01");
        sofia.setEmail("sofia.lopez@deepblue.org");
        sofia.setActive(true);
        sofia.getExpertiseAreas().add(marineBirds);
        sofia.getExpertiseAreas().add(trauma);

        specialistRepository.save(elena);
        specialistRepository.save(mateo);
        specialistRepository.save(sofia);

        List<Specialist> traumaSpecialists = specialistRepository.findActiveByExpertiseName("Trauma");

        assertThat(traumaSpecialists).hasSize(2);
        assertThat(traumaSpecialists).extracting(Specialist::getFirstName)
                .containsExactlyInAnyOrder("Elena", "Sofia");
    }
    @Test
    void testCreateTreatmentsForAnimal() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-TRT-01");
        RescueCase savedCase = rescueCaseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-TRT-01");
        animal.setCommonName("Sea Lion");
        animal.setScientificName("Otariidae");
        animal.setRescueCase(savedCase);
        Animal savedAnimal = animalRepository.save(animal);

        Specialist elena = new Specialist();
        elena.setFirstName("Elena");
        elena.setLastName("Vargas");
        elena.setProfessionalCode("SPEC-EV-03");
        elena.setEmail("elena.v3@deepblue.org");
        elena.setActive(true);

        Specialist mateo = new Specialist();
        mateo.setFirstName("Mateo");
        mateo.setLastName("Gomez");
        mateo.setProfessionalCode("SPEC-MG-02");
        mateo.setEmail("mateo.g2@deepblue.org");
        mateo.setActive(true);

        Specialist savedElena = specialistRepository.save(elena);
        Specialist savedMateo = specialistRepository.save(mateo);

        Treatment t1 = new Treatment();
        t1.setAnimal(savedAnimal);
        t1.setSpecialist(savedElena);
        t1.setType(TreatmentType.WOUND_CARE);
        t1.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 1, 9, 0));

        Treatment t2 = new Treatment();
        t2.setAnimal(savedAnimal);
        t2.setSpecialist(savedElena);
        t2.setType(TreatmentType.HYDRATION);
        t2.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 2, 10, 0));

        Treatment t3 = new Treatment();
        t3.setAnimal(savedAnimal);
        t3.setSpecialist(savedMateo);
        t3.setType(TreatmentType.OBSERVATION);
        t3.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 3, 11, 0));

        Treatment savedT1 = treatmentRepository.save(t1);
        Treatment savedT2 = treatmentRepository.save(t2);
        Treatment savedT3 = treatmentRepository.save(t3);

        assertThat(savedT1.getId()).isNotNull();
        assertThat(savedT2.getId()).isNotNull();
        assertThat(savedT3.getId()).isNotNull();
    }
    @Test
    void testTreatmentsOrderedChronologically() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-TRT-02");
        RescueCase savedCase = rescueCaseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-TRT-02");
        animal.setCommonName("Dolphin");
        animal.setScientificName("Delphinidae");
        animal.setRescueCase(savedCase);
        Animal savedAnimal = animalRepository.save(animal);

        Treatment t1 = new Treatment();
        t1.setAnimal(savedAnimal);
        t1.setType(TreatmentType.WOUND_CARE);
        t1.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 1, 9, 0));

        Treatment t2 = new Treatment();
        t2.setAnimal(savedAnimal);
        t2.setType(TreatmentType.HYDRATION);
        t2.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 2, 10, 0));

        Treatment t3 = new Treatment();
        t3.setAnimal(savedAnimal);
        t3.setType(TreatmentType.OBSERVATION);
        t3.setPerformedAt(java.time.LocalDateTime.of(2026, 8, 3, 11, 0));

        treatmentRepository.save(t1);
        treatmentRepository.save(t2);
        treatmentRepository.save(t3);

        List<Treatment> animalTreatments = treatmentRepository.findByAnimalIdOrderByPerformedAtAsc(savedAnimal.getId());

        assertThat(animalTreatments).hasSize(3);
        assertThat(animalTreatments).extracting(Treatment::getType)
                .containsExactly(TreatmentType.WOUND_CARE, TreatmentType.HYDRATION, TreatmentType.OBSERVATION);
    }
    @Test
    void testQueryByDateInterval() {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-DATE-01");
        RescueCase savedCase = rescueCaseRepository.save(rescueCase);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-DATE-01");
        animal.setCommonName("Pelican");
        animal.setScientificName("Pelecanus");
        animal.setRescueCase(savedCase);
        Animal savedAnimal = animalRepository.save(animal);

        java.time.LocalDateTime date1 = java.time.LocalDateTime.of(2026, 8, 1, 10, 0);
        java.time.LocalDateTime date2 = java.time.LocalDateTime.of(2026, 8, 10, 10, 0);
        java.time.LocalDateTime date3 = java.time.LocalDateTime.of(2026, 8, 20, 10, 0);

        Treatment t1 = new Treatment();
        t1.setAnimal(savedAnimal);
        t1.setPerformedAt(date1);
        t1.setType("CHECK_1");

        Treatment t2 = new Treatment();
        t2.setAnimal(savedAnimal);
        t2.setPerformedAt(date2);
        t2.setType("CHECK_2");

        Treatment t3 = new Treatment();
        t3.setAnimal(savedAnimal);
        t3.setPerformedAt(date3);
        t3.setType("CHECK_3");

        treatmentRepository.save(t1);
        treatmentRepository.save(t2);
        treatmentRepository.save(t3);

        java.time.LocalDateTime startInterval = java.time.LocalDateTime.of(2026, 8, 5, 0, 0);
        java.time.LocalDateTime endInterval = java.time.LocalDateTime.of(2026, 8, 15, 23, 59);

        List<Treatment> filteredTreatments = treatmentRepository.findByPerformedAtBetween(startInterval, endInterval);

        assertThat(filteredTreatments).hasSize(1);
        assertThat(filteredTreatments.get(0).getPerformedAt()).isEqualTo(date2);
    }
@Test
    void testUniqueConstraintViolation() {
        RescueCase rescueCase1 = new RescueCase();
        rescueCase1.setCaseCode("RES-UNIQ-01");
        RescueCase savedCase1 = rescueCaseRepository.save(rescueCase1);

        RescueCase rescueCase2 = new RescueCase();
        rescueCase2.setCaseCode("RES-UNIQ-02");
        RescueCase savedCase2 = rescueCaseRepository.save(rescueCase2);

        Animal animal1 = new Animal();
        animal1.setAnimalCode("AN-100");
        animal1.setCommonName("Dolphin");
        animal1.setScientificName("Delphinidae");
        animal1.setRescueCase(savedCase1);
        animalRepository.saveAndFlush(animal1);

        Animal animal2 = new Animal();
        animal2.setAnimalCode("AN-100");
        animal2.setCommonName("Sea Turtle");
        animal2.setScientificName("Chelonioidea");
        animal2.setRescueCase(savedCase2);

        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> animalRepository.saveAndFlush(animal2)
        );
    }
    @Test
    void testForeignKeyConstraintViolation() {
        Specialist specialist = new Specialist();
        specialist.setFirstName("Carlos");
        specialist.setLastName("Mendoza");
        specialist.setProfessionalCode("SPEC-FK-01");
        specialist.setEmail("carlos.fk@deepblue.org");
        specialist.setActive(true);
        Specialist savedSpecialist = specialistRepository.save(specialist);

        Animal unpersistedAnimal = new Animal();
        unpersistedAnimal.setId(99999L);

        Treatment treatment = new Treatment();
        treatment.setAnimal(unpersistedAnimal);
        treatment.setSpecialist(savedSpecialist);
        treatment.setType(TreatmentType.OBSERVATION);
        treatment.setPerformedAt(java.time.LocalDateTime.now());

        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> treatmentRepository.saveAndFlush(treatment)
        );
    }

    @Test
    void testCheckConstraintViolation() {
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> jdbcTemplate.execute(
                "INSERT INTO rescue_cases (case_code, status) VALUES ('RES-CHK-ERR', 'INVALID_STATUS')"
            )
        );
    }


    
}
