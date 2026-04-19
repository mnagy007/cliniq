package com.cliniq.bootstrap;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit architectural rule tests enforcing Clean Architecture boundaries.
 * 
 * These tests verify that the domain layer remains pure (no Spring/JPA imports),
 * that dependencies flow inward according to the hexagonal architecture, and that
 * infrastructure concerns are properly isolated.
 * 
 * @see <a href="https://www.archunit.org/">ArchUnit Documentation</a>
 */
class ArchRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setup() {
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("com.cliniq");
    }

    /**
     * Rule 1: Domain layer must not import Spring or Jakarta Persistence.
     * 
     * Domain classes should be pure Java with no infrastructure dependencies.
     * This ensures the domain model remains portable and testable in isolation.
     */
    @Test
    void domainLayerMustNotImportSpringOrJpa() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.cliniq.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .because("Domain layer must be pure Java without infrastructure dependencies")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    /**
     * Rule 2: Domain layer must not depend on application layer.
     * 
     * Dependencies should flow inward: domain <- application <- adapters.
     * The domain should have no knowledge of use cases or application services.
     */
    @Test
    void domainLayerMustNotDependOnApplicationLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.cliniq.domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.cliniq.application..")
                .because("Domain layer must not depend on application layer (dependencies flow inward)")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    /**
     * Rule 3: Application layer must not import Spring or Jakarta Persistence.
     * 
     * Application services should be framework-agnostic. They define port interfaces
     * that adapters implement, keeping the application layer decoupled from infrastructure.
     */
    @Test
    void applicationLayerMustNotImportSpringOrJpa() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.cliniq.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .because("Application layer must be framework-agnostic (port/adapter pattern)")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    /**
     * Rule 4: Application layer must not depend on adapter layers.
     * 
     * The application layer should only depend on domain and its own port interfaces.
     * It should have no knowledge of persistence, web, notification, or external adapters.
     */
    @Test
    void applicationLayerMustNotDependOnAdapterLayers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.cliniq.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.cliniq.persistence..",
                        "com.cliniq.web..",
                        "com.cliniq.notification..",
                        "com.cliniq.external.."
                )
                .because("Application layer must not depend on adapter implementations")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    /**
     * Rule 5: TenantContext must only be used in the web layer.
     * 
     * TenantContext is a web-layer concern for extracting tenant from HTTP requests.
     * Domain and application layers should receive TenantId as an explicit parameter,
     * maintaining their independence from HTTP/web concerns.
     */
    @Test
    void tenantContextMustOnlyBeUsedInWebAndBootstrapLayer() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("com.cliniq.web..")
                .and().resideOutsideOfPackage("com.cliniq.bootstrap..")
                .should().dependOnClassesThat()
                .haveSimpleName("TenantContext")
                .because("TenantContext is a web-layer concern; domain/application should receive TenantId explicitly. Bootstrap may use it for wiring (e.g., tenant filter aspect)")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }

    /**
     * Rule 6: JPA entities must reside in the persistence layer.
     * 
     * JPA entity classes (prefixed with "Jpa") are infrastructure concerns
     * and must only exist in the persistence module. The domain layer uses
     * pure Java domain objects without persistence annotations.
     */
    @Test
    void jpaEntitiesMustResideInPersistenceLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameStartingWith("Jpa")
                .should().resideInAPackage("com.cliniq.persistence..")
                .because("JPA entities are persistence concerns and must reside in the persistence layer")
                .allowEmptyShould(true);

        rule.check(allClasses);
    }
}
