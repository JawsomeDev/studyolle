package com.studyolle;


import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String Account = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    private final JavaClasses importedCClasses = new ClassFileImporter().importPackagesOf(App.class);


    @Test
    void validateServiceNaming(){
       ArchRule archRule = classes().that().resideInAPackage("..modules.study..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAnyPackage(STUDY, EVENT);

       archRule.check(importedCClasses);
    }

    @Test
    void validateServiceName(){
        ArchRule archRule = classes().that().resideInAPackage(EVENT)
                .should().accessClassesThat().resideInAnyPackage(STUDY, Account, EVENT);

        archRule.check(importedCClasses);
    }

    @Test
    void validateServiceNameAndVersion(){
        ArchRule archRule = classes().that().resideInAPackage(Account)
                .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, Account);

        archRule.check(importedCClasses);
    }

    @Test
    void cycleCheck(){
        ArchRule archRule = slices().matching("com.studyolle.modules.(*)..")
                .should().beFreeOfCycles();

        archRule.check(importedCClasses);
    }

    @Test
    void modulesPackageRule(){
        ArchRule archRule = classes().that().resideInAPackage("com.studyolle.modules..")
                .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("com.studyolle.modules..");

        archRule.check(importedCClasses);
    }

}
