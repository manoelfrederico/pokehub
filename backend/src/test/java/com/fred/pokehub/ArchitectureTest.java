package com.fred.pokehub;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.fred.pokehub", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	// Rooted at the application package so "web" does not also match org.springframework.web.
	private static final String DOMAIN = "com.fred.pokehub.domain..";
	private static final String APPLICATION = "com.fred.pokehub.application..";
	private static final String INFRASTRUCTURE = "com.fred.pokehub.infrastructure..";
	private static final String WEB = "com.fred.pokehub.web..";
	private static final String CONFIG = "com.fred.pokehub.config..";

	private static final DescribedPredicate<JavaClass> UTIL_HELPER_OR_MANAGER = DescribedPredicate.describe(
			"simple name ending with Util, Helper or Manager",
			javaClass -> {
				String name = javaClass.getSimpleName();
				return name.endsWith("Util") || name.endsWith("Helper") || name.endsWith("Manager");
			});

	private static final String[] FRAMEWORK_PACKAGES = {
			"org.springframework..",
			"org.hibernate..",
			"jakarta..",
			"javax.persistence..",
			"com.fasterxml.jackson..",
			"tools.jackson..",
	};

	@ArchTest
	static final ArchRule domainMustNotDependOnOtherLayers = noClasses()
			.that().resideInAnyPackage(DOMAIN)
			.should().dependOnClassesThat().resideInAnyPackage(APPLICATION, INFRASTRUCTURE, WEB, CONFIG);

	@ArchTest
	static final ArchRule domainAndApplicationMustNotUseFrameworks = noClasses()
			.that().resideInAnyPackage(DOMAIN, APPLICATION)
			.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES);

	@ArchTest
	static final ArchRule applicationMustNotDependOnOuterLayers = noClasses()
			.that().resideInAnyPackage(APPLICATION)
			.should().dependOnClassesThat().resideInAnyPackage(WEB, INFRASTRUCTURE, CONFIG);

	@ArchTest
	static final ArchRule webMustNotDependOnInfrastructure = noClasses()
			.that().resideInAnyPackage(WEB)
			.should().dependOnClassesThat().resideInAnyPackage(INFRASTRUCTURE);

	@ArchTest
	static final ArchRule springSecurityStaysInSecurityAndConfig = noClasses()
			.that().resideOutsideOfPackages("com.fred.pokehub.infrastructure.security..", CONFIG)
			.should().dependOnClassesThat().resideInAnyPackage("org.springframework.security..");

	@ArchTest
	static final ArchRule infrastructureMustNotDependOnWeb = noClasses()
			.that().resideInAnyPackage(INFRASTRUCTURE)
			.should().dependOnClassesThat().resideInAnyPackage(WEB);

	@ArchTest
	static final ArchRule noFieldInjection = noFields()
			.should().beAnnotatedWith(Autowired.class);

	@ArchTest
	static final ArchRule noJavaUtilLogging = noClasses()
			.should().dependOnClassesThat().resideInAnyPackage("java.util.logging..");

	@ArchTest
	static final ArchRule noUtilHelperOrManagerNames = noClasses()
			.that(UTIL_HELPER_OR_MANAGER)
			.should().beEnums();


}
