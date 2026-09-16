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
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.ExpertiseRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.RescueCenterRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import static org.assertj.core.api.Assertions.assertThat;
import jakarta.transaction.Transactional;

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
        record.setInitialWeight(28.40);
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
    
}
