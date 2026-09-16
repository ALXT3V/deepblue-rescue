1. Nombre del proyecto
        DeepBlue Rescue

2. Descripcion breve
    Aplicación en Spring Boot desarrollada para la gestión de rescates, rehabilitación y seguimiento médico de fauna marina, permite administrar centros de atención, casuísticas de rescate, expedientes clínicos, profesionales asignados y los tratamientos realizados.

3. Modelo de datos
    El sistema cuenta con 7 entidades principales persistidas en PostgreSQL:

    RescueCenter: Centro físico donde se reciben y atienden las especies.

    RescueCase: Reporte de rescate registrado en el sistema.

    Animal: Especie marina ingresada al centro. 

    MedicalRecord: Expediente médico inicial del animal.

    Specialist: Profesional de la salud o técnico especialista.

    Expertise: Área de especialidad médica-

    Treatment: Procedimiento médico o atención aplicada.

4. Relaciones
    RescueCenter 1 ───────── N RescueCase


    RescueCase 1 ───────── 1 Animal


    Animal 1 ───────── 1 MedicalRecord


    Specialist N ───────── M Expertise


    Animal 1 ───────── N Treatment


    Specialist 1 ───────── N Treatment

5. Instrucciones para ejecutar
    Requisitos previos:

    Java 21 configurado en la variable JAVA_HOME.

    Docker Desktop en ejecución.

    .\mvnw.cmd spring-boot:run

6. Instrucciones para ejecutar tests

    Las pruebas de integración ejecutan una suite sobre PostgreSQL en un contenedor Docker.

    Comando para correr la suite de pruebas:
    .\mvnw.cmd clean test

7. Explicación de Flyway
    Flyway gestiona las migraciones y el control de versiones de la base de datos a través de archivos SQL secuenciales ubicados en src/main/resources/db/migration/. 
    Garantiza que el esquema en PostgreSQL sea idéntico entre entornos, trabaja en conjunto con spring.jpa.hibernate.ddl-auto: validate, impidiendo que Hibernate altere las tablas y limitándose a validar que las entidades Java concuerden exactamente con las migraciones ejecutadas.

8. Explicación de Testcontainers

    Testcontainers es una librería de Java que levanta contenedores Docker efímeros durante la ejecución de las pruebas integradas (PersistenceIntegrationTest.java). Permite probar la persistencia sobre un motor de base de datos PostgreSQL real e idéntico al de producción, descartando la necesidad de usar bases de datos en memoria y eliminando diferencias de sintaxis SQL.

9. Listado de Query Methods implementados
    RescueCaseRepository

        - findByCaseCode(String caseCode)

        - findByStatus(RescueStatus status)

    AnimalRepository

    - findByRescueCaseRescueCenterCode(String centerCode)

    - findByCommonNameContainingIgnoreCase(String commonName)

    TreatmentRepository

    - findByAnimalAnimalCodeOrderByPerformedAtAsc(String animalCode)

    - findByPerformedAtBetweenOrderByPerformedAtAsc(LocalDateTime start, LocalDateTime end)

10. Listado de consultas JPQL implementadas
    SpecialistRepository
    SELECT DISTINCT s
FROM Specialist s
JOIN s.expertiseAreas e
WHERE LOWER(e.name) = LOWER(:expertiseName)

    TreatmentRepository
    SELECT t
FROM Treatment t
JOIN t.specialist s
JOIN s.expertiseAreas e
WHERE LOWER(e.name) = LOWER(:expertiseName)

    AnimalRepository
    SELECT DISTINCT a
FROM Animal a
JOIN a.rescueCase rc
JOIN a.treatments t
JOIN t.specialist s
JOIN s.expertiseAreas e
WHERE rc.status = :status
  AND LOWER(e.name) = LOWER(:expertiseName)