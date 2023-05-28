package com.studyolle;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    // modules 패키지는 modules 에서만 참조를 할 수 있다.
    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.studyolle.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.studyolle.modules..");
    // Study 패키지에 접근 하는 클래스들은 오직 STUDY와 EVENT 에 있는 패키지에 있는 클래스들만 접근이 가능하다.
    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT);
    // Event 패키지 클래스 들은 EVENT , ACCOUNT, STUDY 패키지 클래스들에 접근 해야 한다.
    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(EVENT, ACCOUNT, STUDY);
    // Account 패키지 클래스 들은 ACCOUNT, ZONE, TAG 패키지 클래스들에 접근 해야 한다.
    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(ACCOUNT, ZONE, TAG);
    // 순환참조 확인
    @ArchTest
    ArchRule cycleCheck = slices().matching("com.studyolle.modules.(*)..")
            .should().beFreeOfCycles();
}
