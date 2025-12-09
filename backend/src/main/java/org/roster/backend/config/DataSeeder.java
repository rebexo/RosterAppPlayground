package org.roster.backend.config;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.out.persistence.*;
import org.roster.backend.domain.Shift;
import org.roster.backend.domain.TemplateShift;
import org.roster.backend.domain.User;
import org.roster.backend.domain.WeeklyTemplate;
import org.roster.backend.domain.enums.Role;
import org.roster.backend.adapter.out.persistence.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.roster.backend.domain.*;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth; // Für den nächsten Monat
import java.util.UUID;

@Component
@Profile("dev") // nur im dev-Profile werden DAten angelegt
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final WeeklyTemplateRepository weeklyTemplateRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScheduleSchemaRepository scheduleSchemaRepository;
    private final AvailabilityEntryRepository availabilityEntryRepository;
    // Optional, aber sauberer fürs Löschen:
    private final SchemaTemplateAssignmentRepository schemaTemplateAssignmentRepository;
    private final AvailabilityDetailRepository availabilityDetailRepository;
    private final TemplateShiftRepository templateShiftRepository; // Für sauberes Template-Löschen

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- Starting Data Seeder ---");

        // 1. Bestehende Daten löschen, um einen sauberen Start zu garantieren
        availabilityDetailRepository.deleteAll();
        availabilityEntryRepository.deleteAll();
        schemaTemplateAssignmentRepository.deleteAll();
        scheduleSchemaRepository.deleteAll();
        templateShiftRepository.deleteAll(); // Shifts innerhalb von Templates löschen
        weeklyTemplateRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();
        weeklyTemplateRepository.deleteAll();
        shiftRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Test-User anlegen
        User devUser = new User();
        devUser.setUsername("Rebecca");
        devUser.setPassword(passwordEncoder.encode("Password123!"));
        devUser.setRole(Role.PLANNER);
        userRepository.save(devUser);

        System.out.println("Created User: " + devUser.getUsername());

        User devUser2 = new User();
        devUser2.setUsername("Louise");
        devUser2.setPassword(passwordEncoder.encode("Password123!"));
        devUser2.setRole(Role.PLANNER);
        userRepository.save(devUser2);

        System.out.println("Created User: " + devUser2.getUsername());


        // 3. Basis-Schichten anlegen
        Shift fruehCafe = new Shift();
        fruehCafe.setName("Frühschicht");
        fruehCafe.setStartTime(LocalTime.of(6, 0));
        fruehCafe.setEndTime(LocalTime.of(14, 0));
        fruehCafe.setPlanner(devUser);

        Shift grauCafe = new Shift();
        grauCafe.setName("Knecht");
        grauCafe.setStartTime(LocalTime.of(10, 30));
        grauCafe.setEndTime(LocalTime.of(17, 0));
        grauCafe.setPlanner(devUser);

        Shift spaetCafe = new Shift();
        spaetCafe.setName("Spätschicht");
        spaetCafe.setStartTime(LocalTime.of(14, 0));
        spaetCafe.setEndTime(LocalTime.of(22, 0));
        spaetCafe.setPlanner(devUser);

        Shift spaetBar = new Shift();
        spaetBar.setName("Spätschicht");
        spaetBar.setStartTime(LocalTime.of(20, 0));
        spaetBar.setEndTime(LocalTime.of(23, 59));
        spaetBar.setPlanner(devUser);

        Shift fruehGinst = new Shift();
        fruehGinst.setName("Frühschicht Musenhain");
        fruehGinst.setStartTime(LocalTime.of(16, 30));
        fruehGinst.setEndTime(LocalTime.of(1, 0));
        fruehGinst.setPlanner(devUser);

        Shift spaetGinst = new Shift();
        spaetGinst.setName("Spätschicht Musenhain");
        spaetGinst.setStartTime(LocalTime.of(19, 30));
        spaetGinst.setEndTime(LocalTime.of(3, 0));
        spaetGinst.setPlanner(devUser);

        Shift grauGinst = new Shift();
        grauGinst.setName("Hilfsschicht Musenhain");
        grauGinst.setStartTime(LocalTime.of(20, 0));
        grauGinst.setEndTime(LocalTime.of(23, 30));
        grauGinst.setPlanner(devUser);

        shiftRepository.saveAll(List.of(fruehCafe,grauCafe, spaetCafe, spaetBar, fruehGinst, spaetGinst, grauGinst));
        System.out.println("Created " + shiftRepository.count() + " Shifts.");

        // 4. Standard-Template anlegen
        WeeklyTemplate standardWoche = new WeeklyTemplate();
        standardWoche.setName("Sommerwoche Cafe");
        standardWoche.setDescription("Normale Belegung für die Sommermonate");
        standardWoche.setPlanner(devUser);

        TemplateShift montagFrueh = new TemplateShift();
        montagFrueh.setWeekday(0);
        montagFrueh.setPositionName("Kaffee");
        montagFrueh.setShift(fruehCafe);
        montagFrueh.setTemplate(standardWoche);

        TemplateShift montagFrueh2 = new TemplateShift();
        montagFrueh2.setWeekday(0);
        montagFrueh2.setPositionName("Küche");
        montagFrueh2.setShift(fruehCafe);
        montagFrueh2.setTemplate(standardWoche);

        TemplateShift montagFrueh3 = new TemplateShift();
        montagFrueh3.setWeekday(0);
        montagFrueh3.setPositionName("Grau");
        montagFrueh3.setShift(grauCafe);
        montagFrueh3.setTemplate(standardWoche);

        TemplateShift dienstagSpaet = new TemplateShift();
        dienstagSpaet.setWeekday(1);
        dienstagSpaet.setPositionName("Di-Kaffee");
        dienstagSpaet.setShift(fruehCafe);
        dienstagSpaet.setTemplate(standardWoche);

        TemplateShift mifrueh = new TemplateShift();
        mifrueh.setWeekday(2);
        mifrueh.setPositionName("Mi-Kaffee");
        mifrueh.setShift(fruehCafe);
        mifrueh.setTemplate(standardWoche);

        TemplateShift dofrueh = new TemplateShift();
        dofrueh.setWeekday(3);
        dofrueh.setPositionName("Do-Kaffee");
        dofrueh.setShift(fruehCafe);
        dofrueh.setTemplate(standardWoche);

        TemplateShift frfrueh = new TemplateShift();
        frfrueh.setWeekday(4);
        frfrueh.setPositionName("Fr-Kaffee");
        frfrueh.setShift(fruehCafe);
        frfrueh.setTemplate(standardWoche);

        TemplateShift safrueh = new TemplateShift();
        safrueh.setWeekday(5);
        safrueh.setPositionName("Sa-Kaffee");
        safrueh.setShift(fruehCafe);
        safrueh.setTemplate(standardWoche);

        TemplateShift sofrueh = new TemplateShift();
        sofrueh.setWeekday(6);
        sofrueh.setPositionName("So-Kaffee");
        sofrueh.setShift(fruehCafe);
        sofrueh.setTemplate(standardWoche);

        standardWoche.getShifts().addAll(List.of(montagFrueh,montagFrueh2, montagFrueh3,dienstagSpaet, mifrueh, dofrueh, frfrueh, safrueh, sofrueh));

        weeklyTemplateRepository.save(standardWoche);
        System.out.println("Created Template: " + standardWoche.getName());

        WeeklyTemplate standardWocheBar = new WeeklyTemplate();
        standardWocheBar.setName("Defaultbelegung Bar");
        standardWocheBar.setDescription("Normale Wochenbelegung für die Bar");
        standardWocheBar.setPlanner(devUser);

        TemplateShift mo = new TemplateShift();
        mo.setWeekday(0);
        mo.setPositionName("Barkeeper");
        mo.setShift(spaetBar);
        mo.setTemplate(standardWocheBar);

        TemplateShift di = new TemplateShift();
        di.setWeekday(1);
        di.setPositionName("Barkeeper");
        di.setShift(spaetBar);
        di.setTemplate(standardWocheBar);

        TemplateShift mi = new TemplateShift();
        mi.setWeekday(2);
        mi.setPositionName("Barkeeper");
        mi.setShift(spaetBar);
        mi.setTemplate(standardWocheBar);

        TemplateShift don = new TemplateShift();
        don.setWeekday(3);
        don.setPositionName("Barkeeper");
        don.setShift(spaetBar);
        don.setTemplate(standardWocheBar);

        TemplateShift fr = new TemplateShift();
        fr.setWeekday(4);
        fr.setPositionName("Barkeeper");
        fr.setShift(spaetBar);
        fr.setTemplate(standardWocheBar);

        TemplateShift sa = new TemplateShift();
        sa.setWeekday(5);
        sa.setPositionName("Barkeeper");
        sa.setShift(spaetBar);
        sa.setTemplate(standardWocheBar);

        TemplateShift so = new TemplateShift();
        so.setWeekday(6);
        so.setPositionName("Barkeeper");
        so.setShift(spaetBar);
        so.setTemplate(standardWocheBar);

        standardWocheBar.getShifts().addAll(List.of(mo, di, mi, don, fr, sa, so));

        weeklyTemplateRepository.save(standardWocheBar);

        WeeklyTemplate standardWocheGinst = new WeeklyTemplate();
        standardWocheGinst.setName("Standardwoche Musenhain");
        standardWocheGinst.setDescription("Normale Belegung");
        standardWocheGinst.setPlanner(devUser);

        TemplateShift moGinst = new TemplateShift();
        moGinst.setWeekday(0);
        moGinst.setPositionName("Barkeeper allein");
        moGinst.setShift(fruehGinst);
        moGinst.setTemplate(standardWocheGinst);

        TemplateShift diGinst = new TemplateShift();
        diGinst.setWeekday(1);
        diGinst.setPositionName("Barkeeper allein");
        diGinst.setShift(fruehGinst);
        diGinst.setTemplate(standardWocheGinst);

        TemplateShift miGinst = new TemplateShift();
        miGinst.setWeekday(2);
        miGinst.setPositionName("Barkeeper allein");
        miGinst.setShift(fruehGinst);
        miGinst.setTemplate(standardWocheGinst);

        TemplateShift doFruehGinst = new TemplateShift();
        doFruehGinst.setWeekday(3);
        doFruehGinst.setPositionName("Barkeeper früh");
        doFruehGinst.setShift(fruehGinst);
        doFruehGinst.setTemplate(standardWocheGinst);

        TemplateShift doSpaetGinst = new TemplateShift();
        doSpaetGinst.setWeekday(3);
        doSpaetGinst.setPositionName("Barkeeper spät");
        doSpaetGinst.setShift(spaetGinst);
        doSpaetGinst.setTemplate(standardWocheGinst);

        TemplateShift frFruehGinst = new TemplateShift();
        frFruehGinst.setWeekday(4);
        frFruehGinst.setPositionName("Barkeeper früh");
        frFruehGinst.setShift(fruehGinst);
        frFruehGinst.setTemplate(standardWocheGinst);

        TemplateShift frSpaetGinst = new TemplateShift();
        frSpaetGinst.setWeekday(4);
        frSpaetGinst.setPositionName("Barkeeper spät");
        frSpaetGinst.setShift(spaetGinst);
        frSpaetGinst.setTemplate(standardWocheGinst);

        TemplateShift frGrauGinst = new TemplateShift();
        frGrauGinst.setWeekday(4);
        frGrauGinst.setPositionName("Grau");
        frGrauGinst.setShift(grauGinst);
        frGrauGinst.setTemplate(standardWocheGinst);

        TemplateShift saFruehGinst = new TemplateShift();
        saFruehGinst.setWeekday(5);
        saFruehGinst.setPositionName("Barkeeper früh");
        saFruehGinst.setShift(fruehGinst);
        saFruehGinst.setTemplate(standardWocheGinst);

        TemplateShift saSpaetGinst = new TemplateShift();
        saSpaetGinst.setWeekday(5);
        saSpaetGinst.setPositionName("Barkeeper spät");
        saSpaetGinst.setShift(spaetGinst);
        saSpaetGinst.setTemplate(standardWocheGinst);

        TemplateShift saGrauGinst = new TemplateShift();
        saGrauGinst.setWeekday(5);
        saGrauGinst.setPositionName("Grau");
        saGrauGinst.setShift(grauGinst);
        saGrauGinst.setTemplate(standardWocheGinst);

        TemplateShift soGinst = new TemplateShift();
        soGinst.setWeekday(6);
        soGinst.setPositionName("Barkeeper allein");
        soGinst.setShift(fruehGinst);
        soGinst.setTemplate(standardWocheGinst);

        standardWocheGinst.getShifts().addAll(List.of(moGinst, diGinst, miGinst, doFruehGinst, doSpaetGinst, frFruehGinst, frSpaetGinst, frGrauGinst, saFruehGinst, saSpaetGinst, saGrauGinst, soGinst));

        weeklyTemplateRepository.save(standardWocheGinst);


        // 5. Ein ScheduleSchema für den nächsten Monat erstellen
        System.out.println("Creating ScheduleSchema Musenhain...");
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate startDate = nextMonth.atDay(1);
        LocalDate endDate = nextMonth.atEndOfMonth();

        ScheduleSchema novemberSchema = new ScheduleSchema();
        novemberSchema.setName("Dienstplan " + nextMonth.getMonth().name() + " " + nextMonth.getYear());
        novemberSchema.setStartDate(startDate);
        novemberSchema.setEndDate(endDate);
        novemberSchema.setPlanner(devUser);
        novemberSchema.setExpectedEntries(5); // Beispiel: Erwarte 5 Antworten
        novemberSchema.setAvailabilityLinkID(UUID.randomUUID().toString().replace("-", "").substring(0, 12)); // Kürzerer Link

        // Eine Template-Zuweisung für das erstellte Schema
        SchemaTemplateAssignment assignment = new SchemaTemplateAssignment();
        assignment.setTemplate(standardWoche);
        assignment.setSchema(novemberSchema);
        assignment.setValidFrom(startDate);
        assignment.setValidTo(endDate);

        novemberSchema.getTemplateAssignments().add(assignment);

        scheduleSchemaRepository.save(novemberSchema);

        System.out.println("Created ScheduleSchema: " + novemberSchema.getName() + " (ID: " + novemberSchema.getId() + ")");

        // noch ein ScheduleSchema für den nächsten Monat erstellen
        System.out.println("Creating ScheduleSchema Ginst...");
        YearMonth nextMonthGinst = YearMonth.now().plusMonths(1);
        LocalDate startDateGinst = nextMonth.atDay(1);
        LocalDate endDateGinst = nextMonth.atEndOfMonth();

        ScheduleSchema dezemberSchemaGinst = new ScheduleSchema();
        dezemberSchemaGinst.setName("Dienstplan Ginst " + nextMonthGinst.getMonth().name() + " " + nextMonthGinst.getYear());
        dezemberSchemaGinst.setStartDate(startDateGinst);
        dezemberSchemaGinst.setEndDate(endDateGinst);
        dezemberSchemaGinst.setPlanner(devUser);
        dezemberSchemaGinst.setExpectedEntries(8); // Beispiel: Erwarte 5 Antworten
        dezemberSchemaGinst.setAvailabilityLinkID(UUID.randomUUID().toString().replace("-", "").substring(0, 12)); // Kürzerer Link

        // Eine Template-Zuweisung für das erstellte Schema
        SchemaTemplateAssignment assignmentGinst = new SchemaTemplateAssignment();
        assignmentGinst.setTemplate(standardWocheGinst);
        assignmentGinst.setSchema(dezemberSchemaGinst);
        assignmentGinst.setValidFrom(startDateGinst);
        assignmentGinst.setValidTo(endDateGinst);

        novemberSchema.getTemplateAssignments().add(assignmentGinst);

        scheduleSchemaRepository.save(dezemberSchemaGinst);

        System.out.println("Created ScheduleSchema: " + dezemberSchemaGinst.getName() + " (ID: " + dezemberSchemaGinst.getId() + ")");

        // 6. Einige AvailabilityEntries erstellen
        System.out.println("Creating AvailabilityEntries...");

        // Eintrag 1: Ford Prefect
        AvailabilityEntry entryMax = new AvailabilityEntry();
        entryMax.setStaffName("Ford Prefect");
        entryMax.setSchema(novemberSchema);
        entryMax.setComment("Kann am 10.11. erst ab 15 Uhr.");
        entryMax.setTargetShiftCount(8); // Beispiel

        AvailabilityDetail detailMax1 = new AvailabilityDetail();
        detailMax1.setDate(startDate.plusDays(3)); // 4. November
        detailMax1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailMax1.setEntry(entryMax);

        AvailabilityDetail detailMax2 = new AvailabilityDetail();
        detailMax2.setDate(startDate.plusDays(4)); // 5. November
        detailMax2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailMax2.setEntry(entryMax);

        entryMax.getDetails().addAll(List.of(detailMax1, detailMax2));
        availabilityEntryRepository.save(entryMax);

        // Eintrag 2: Frida Kahlo
        AvailabilityEntry entryErika = new AvailabilityEntry();
        entryErika.setStaffName("Frida Kahlo");
        entryErika.setSchema(novemberSchema);
        entryErika.setComment("Bevorzuge Wochenenden");
        entryErika.setTargetShiftCount(6);

        AvailabilityDetail detailErika1 = new AvailabilityDetail();
        detailErika1.setDate(startDate.plusDays(9)); // 10. November
        detailErika1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailErika1.setEntry(entryErika);

        entryErika.getDetails().add(detailErika1);
        availabilityEntryRepository.save(entryErika);


        // Eintrag 1 Ginst: Korbi
        AvailabilityEntry entryKorbi = new AvailabilityEntry();
        entryKorbi.setStaffName("Korbi");
        entryKorbi.setSchema(dezemberSchemaGinst);
        entryKorbi.setComment("Kann am 10.11. erst ab 15 Uhr.");
        entryKorbi.setTargetShiftCount(8);

        AvailabilityDetail detailKorbi1 = new AvailabilityDetail();
        detailKorbi1.setDate(startDate.plusDays(3)); // 4. Dezember
        detailKorbi1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailKorbi1.setEntry(entryKorbi);

        AvailabilityDetail detailKorbi2 = new AvailabilityDetail();
        detailKorbi2.setDate(startDate.plusDays(4)); // 5. Dezember
        detailKorbi2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailKorbi2.setEntry(entryKorbi);

        AvailabilityDetail detailKorbi3 = new AvailabilityDetail();
        detailKorbi3.setDate(startDate.plusDays(5)); // 6. Dezember
        detailKorbi3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailKorbi3.setEntry(entryKorbi);

        entryKorbi.getDetails().addAll(List.of(detailKorbi1, detailKorbi2, detailKorbi3));
        availabilityEntryRepository.save(entryKorbi);

        // Eintrag 2 Ginst: Flora
        AvailabilityEntry entryFlora = new AvailabilityEntry();
        entryFlora.setStaffName("Flora");
        entryFlora.setSchema(dezemberSchemaGinst);
        entryFlora.setComment("Sorry, viel weg diesen Monat");
        entryFlora.setTargetShiftCount(5);

        AvailabilityDetail detailFlora1 = new AvailabilityDetail();
        detailFlora1.setDate(startDate); // 1. Dezember
        detailFlora1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora1.setEntry(entryFlora);

        AvailabilityDetail detailFlora2 = new AvailabilityDetail();
        detailFlora2.setDate(startDate.plusDays(1)); // 2. Dezember
        detailFlora2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora2.setEntry(entryFlora);

        AvailabilityDetail detailFlora3 = new AvailabilityDetail();
        detailFlora3.setDate(startDate.plusDays(2)); // 3. Dezember
        detailFlora3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora3.setEntry(entryFlora);

        AvailabilityDetail detailFlora4 = new AvailabilityDetail();
        detailFlora4.setDate(startDate.plusDays(7)); // 8. Dezember
        detailFlora4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora4.setEntry(entryFlora);

        AvailabilityDetail detailFlora5 = new AvailabilityDetail();
        detailFlora5.setDate(startDate.plusDays(8)); // 9. Dezember
        detailFlora5.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora5.setEntry(entryFlora);

        AvailabilityDetail detailFlora6 = new AvailabilityDetail();
        detailFlora6.setDate(startDate.plusDays(14)); // 15. Dezember
        detailFlora6.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora6.setEntry(entryFlora);

        AvailabilityDetail detailFlora7 = new AvailabilityDetail();
        detailFlora7.setDate(startDate.plusDays(15)); // 16. Dezember
        detailFlora7.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora7.setEntry(entryFlora);

        AvailabilityDetail detailFlora8 = new AvailabilityDetail();
        detailFlora8.setDate(startDate.plusDays(16)); // 17. Dezember
        detailFlora8.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora8.setEntry(entryFlora);

        AvailabilityDetail detailFlora9 = new AvailabilityDetail();
        detailFlora9.setDate(startDate.plusDays(9)); // 10. Dezember
        detailFlora9.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora9.setEntry(entryFlora);

        AvailabilityDetail detailFlora10 = new AvailabilityDetail();
        detailFlora10.setDate(startDate.plusDays(21)); // 22. Dezember
        detailFlora10.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora10.setEntry(entryFlora);

        AvailabilityDetail detailFlora11 = new AvailabilityDetail();
        detailFlora11.setDate(startDate.plusDays(22)); // 23. Dezember
        detailFlora11.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora11.setEntry(entryFlora);

        AvailabilityDetail detailFlora12 = new AvailabilityDetail();
        detailFlora12.setDate(startDate.plusDays(23)); // 24. Dezember
        detailFlora12.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora12.setEntry(entryFlora);

        AvailabilityDetail detailFlora13 = new AvailabilityDetail();
        detailFlora13.setDate(startDate.plusDays(28)); // 29. Dezember
        detailFlora13.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora13.setEntry(entryFlora);

        AvailabilityDetail detailFlora14 = new AvailabilityDetail();
        detailFlora14.setDate(startDate.plusDays(29)); // 30. Dezember
        detailFlora14.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora14.setEntry(entryFlora);

        AvailabilityDetail detailFlora15 = new AvailabilityDetail();
        detailFlora15.setDate(startDate.plusDays(30)); // 31. Dezember
        detailFlora15.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFlora15.setEntry(entryFlora);


        entryFlora.getDetails().addAll(List.of(detailFlora1, detailFlora2, detailFlora3, detailFlora4, detailFlora5,
                detailFlora6, detailFlora7, detailFlora8, detailFlora9, detailFlora10, detailFlora11,
                detailFlora12, detailFlora13, detailFlora14, detailFlora15));
        availabilityEntryRepository.save(entryFlora);


        // Eintrag 3 Ginst : Nymia
        AvailabilityEntry entryNymia = new AvailabilityEntry();
        entryNymia.setStaffName("Nymia");
        entryNymia.setSchema(dezemberSchemaGinst);
        entryNymia.setComment("Chsristmas is coming");
        entryNymia.setTargetShiftCount(12);

        AvailabilityDetail detailNymia1 = new AvailabilityDetail();
        detailNymia1.setDate(startDate.plusDays(5)); // 6. Dezember
        detailNymia1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailNymia1.setEntry(entryNymia);

        AvailabilityDetail detailNymia2 = new AvailabilityDetail();
        detailNymia2.setDate(startDate.plusDays(9)); // 10. Dezember
        detailNymia2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailNymia2.setEntry(entryNymia);

        AvailabilityDetail detailNymia3 = new AvailabilityDetail();
        detailNymia3.setDate(startDate.plusDays(17)); // 18. Dezember
        detailNymia3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailNymia3.setEntry(entryNymia);

        AvailabilityDetail detailNymia4 = new AvailabilityDetail();
        detailNymia4.setDate(startDate.plusDays(24)); // 25. Dezember
        detailNymia4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailNymia4.setEntry(entryNymia);

        entryNymia.getDetails().addAll(List.of(detailNymia1, detailNymia2, detailNymia3, detailNymia4));
        availabilityEntryRepository.save(entryNymia);

        // Eintrag 4 Ginst: Loy
        AvailabilityEntry entryLoy = new AvailabilityEntry();
        entryLoy.setStaffName("Loy");
        entryLoy.setSchema(dezemberSchemaGinst);
        entryLoy.setComment("Loyle loyle loyle");
        entryLoy.setTargetShiftCount(8);

        AvailabilityDetail detailLoy1 = new AvailabilityDetail();
        detailLoy1.setDate(startDate.plusDays(1)); // 2. Dezember
        detailLoy1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy1.setEntry(entryLoy);

        AvailabilityDetail detailLoy2 = new AvailabilityDetail();
        detailLoy2.setDate(startDate.plusDays(2)); // 3. Dezember
        detailLoy2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy2.setEntry(entryLoy);

        AvailabilityDetail detailLoy3 = new AvailabilityDetail();
        detailLoy3.setDate(startDate.plusDays(3)); // 4. Dezember
        detailLoy3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy3.setEntry(entryLoy);

        AvailabilityDetail detailLoy4 = new AvailabilityDetail();
        detailLoy4.setDate(startDate.plusDays(4)); // 5. Dezember
        detailLoy4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy4.setEntry(entryLoy);

        AvailabilityDetail detailLoy5 = new AvailabilityDetail();
        detailLoy5.setDate(startDate.plusDays(5)); // 6. Dezember
        detailLoy5.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy5.setEntry(entryLoy);

        AvailabilityDetail detailLoy6 = new AvailabilityDetail();
        detailLoy6.setDate(startDate.plusDays(10)); // 11. Dezember
        detailLoy6.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy6.setEntry(entryLoy);

        AvailabilityDetail detailLoy7 = new AvailabilityDetail();
        detailLoy7.setDate(startDate.plusDays(13)); // 14.. Dezember
        detailLoy7.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy7.setEntry(entryLoy);

        AvailabilityDetail detailLoy8 = new AvailabilityDetail();
        detailLoy8.setDate(startDate.plusDays(14)); // 15. Dezember
        detailLoy8.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy8.setEntry(entryLoy);

        AvailabilityDetail detailLoy9 = new AvailabilityDetail();
        detailLoy9.setDate(startDate.plusDays(15)); // 16. Dezember
        detailLoy9.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy9.setEntry(entryLoy);

        AvailabilityDetail detailLoy10 = new AvailabilityDetail();
        detailLoy10.setDate(startDate.plusDays(16)); // 17. Dezember
        detailLoy10.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy10.setEntry(entryLoy);

        AvailabilityDetail detailLoy11 = new AvailabilityDetail();
        detailLoy11.setDate(startDate.plusDays(24)); // 25. Dezember
        detailLoy11.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy11.setEntry(entryLoy);

        AvailabilityDetail detailLoy12 = new AvailabilityDetail();
        detailLoy12.setDate(startDate.plusDays(25)); // 26. Dezember
        detailLoy12.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy12.setEntry(entryLoy);

        AvailabilityDetail detailLoy13 = new AvailabilityDetail();
        detailLoy13.setDate(startDate.plusDays(26)); // 27. Dezember
        detailLoy13.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailLoy13.setEntry(entryLoy);


        entryLoy.getDetails().addAll(List.of(detailLoy1, detailLoy2, detailLoy3, detailLoy4, detailLoy5,
                detailLoy6, detailLoy7, detailLoy8, detailLoy9, detailLoy10, detailLoy11,
                detailLoy12, detailLoy13));
        availabilityEntryRepository.save(entryLoy);


        // Eintrag 5 Ginst: Fritzie
        AvailabilityEntry entryFritzie = new AvailabilityEntry();
        entryFritzie.setStaffName("Fritzie");
        entryFritzie.setSchema(dezemberSchemaGinst);
        entryFritzie.setComment("Fritziele Fritziele Fritziele");
        entryFritzie.setTargetShiftCount(8);

        AvailabilityDetail detailFritzie0 = new AvailabilityDetail();
        detailFritzie0.setDate(startDate); // 1. Dezember
        detailFritzie0.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie0.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie1 = new AvailabilityDetail();
        detailFritzie1.setDate(startDate.plusDays(1)); // 2. Dezember
        detailFritzie1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie1.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie2 = new AvailabilityDetail();
        detailFritzie2.setDate(startDate.plusDays(2)); // 3. Dezember
        detailFritzie2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie2.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie3 = new AvailabilityDetail();
        detailFritzie3.setDate(startDate.plusDays(3)); // 4. Dezember
        detailFritzie3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie3.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie4 = new AvailabilityDetail();
        detailFritzie4.setDate(startDate.plusDays(4)); // 5. Dezember
        detailFritzie4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie4.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie5 = new AvailabilityDetail();
        detailFritzie5.setDate(startDate.plusDays(5)); // 6. Dezember
        detailFritzie5.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie5.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie6 = new AvailabilityDetail();
        detailFritzie6.setDate(startDate.plusDays(9)); // 10. Dezember
        detailFritzie6.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie6.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie7 = new AvailabilityDetail();
        detailFritzie7.setDate(startDate.plusDays(13)); // 14.. Dezember
        detailFritzie7.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie7.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie8 = new AvailabilityDetail();
        detailFritzie8.setDate(startDate.plusDays(14)); // 15. Dezember
        detailFritzie8.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie8.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie9 = new AvailabilityDetail();
        detailFritzie9.setDate(startDate.plusDays(15)); // 16. Dezember
        detailFritzie9.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie9.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie10 = new AvailabilityDetail();
        detailFritzie10.setDate(startDate.plusDays(16)); // 17. Dezember
        detailFritzie10.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie10.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie11 = new AvailabilityDetail();
        detailFritzie11.setDate(startDate.plusDays(24)); // 25. Dezember
        detailFritzie11.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie11.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie12 = new AvailabilityDetail();
        detailFritzie12.setDate(startDate.plusDays(25)); // 26. Dezember
        detailFritzie12.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie12.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie13 = new AvailabilityDetail();
        detailFritzie13.setDate(startDate.plusDays(23)); // 24. Dezember
        detailFritzie13.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie13.setEntry(entryFritzie);

        AvailabilityDetail detailFritzie14 = new AvailabilityDetail();
        detailFritzie14.setDate(startDate.plusDays(6)); // 7. Dezember
        detailFritzie14.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailFritzie14.setEntry(entryFritzie);

        entryFritzie.getDetails().addAll(List.of(detailFritzie0, detailFritzie1, detailFritzie2, detailFritzie3, detailFritzie4, detailFritzie5,
                detailFritzie6, detailFritzie7, detailFritzie8, detailFritzie9, detailFritzie10, detailFritzie11,
                detailFritzie12, detailFritzie13,detailFritzie14));
        availabilityEntryRepository.save(entryFritzie);

        // Eintrag 6 Ginst: Jamie
        AvailabilityEntry entryJamie = new AvailabilityEntry();
        entryJamie.setStaffName("Jamie");
        entryJamie.setSchema(dezemberSchemaGinst);
        entryJamie.setComment("Jamiele Jamiele Jamiele");
        entryJamie.setTargetShiftCount(10);

        AvailabilityDetail detailJamie1 = new AvailabilityDetail();
        detailJamie1.setDate(startDate); // 1. Dezember
        detailJamie1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie1.setEntry(entryJamie);

        AvailabilityDetail detailJamie2 = new AvailabilityDetail();
        detailJamie2.setDate(startDate.plusDays(2)); // 3. Dezember
        detailJamie2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie2.setEntry(entryJamie);

        AvailabilityDetail detailJamie3 = new AvailabilityDetail();
        detailJamie3.setDate(startDate.plusDays(3)); // 4. Dezember
        detailJamie3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie3.setEntry(entryJamie);

        AvailabilityDetail detailJamie4 = new AvailabilityDetail();
        detailJamie4.setDate(startDate.plusDays(4)); // 5. Dezember
        detailJamie4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie4.setEntry(entryJamie);

        AvailabilityDetail detailJamie5 = new AvailabilityDetail();
        detailJamie5.setDate(startDate.plusDays(9)); // 10. Dezember
        detailJamie5.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie5.setEntry(entryJamie);

        AvailabilityDetail detailJamie6 = new AvailabilityDetail();
        detailJamie6.setDate(startDate.plusDays(10)); // 11. Dezember
        detailJamie6.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie6.setEntry(entryJamie);

        AvailabilityDetail detailJamie7 = new AvailabilityDetail();
        detailJamie7.setDate(startDate.plusDays(11)); // 12. Dezember
        detailJamie7.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie7.setEntry(entryJamie);

        AvailabilityDetail detailJamie8 = new AvailabilityDetail();
        detailJamie8.setDate(startDate.plusDays(13)); // 14. Dezember
        detailJamie8.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie8.setEntry(entryJamie);

        AvailabilityDetail detailJamie9 = new AvailabilityDetail();
        detailJamie9.setDate(startDate.plusDays(18)); // 19. Dezember
        detailJamie9.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailJamie9.setEntry(entryJamie);


        entryJamie.getDetails().addAll(List.of(detailJamie1, detailJamie2, detailJamie3, detailJamie4, detailJamie5,
                detailJamie6, detailJamie7, detailJamie8, detailJamie9));
        availabilityEntryRepository.save(entryJamie);

        // Eintrag 7 Ginst: Daniel
        AvailabilityEntry entryDaniel = new AvailabilityEntry();
        entryDaniel.setStaffName("Daniel");
        entryDaniel.setSchema(dezemberSchemaGinst);
        entryDaniel.setComment("Danielle Danielle Danielle");
        entryDaniel.setTargetShiftCount(5);

        AvailabilityDetail detailDaniel1 = new AvailabilityDetail();
        detailDaniel1.setDate(startDate.plusDays(8)); // 9. Dezember
        detailDaniel1.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel1.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel2 = new AvailabilityDetail();
        detailDaniel2.setDate(startDate.plusDays(21)); // 22. Dezember
        detailDaniel2.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel2.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel3 = new AvailabilityDetail();
        detailDaniel3.setDate(startDate.plusDays(22)); // 23. Dezember
        detailDaniel3.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel3.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel4 = new AvailabilityDetail();
        detailDaniel4.setDate(startDate.plusDays(23)); // 24. Dezember
        detailDaniel4.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel4.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel5 = new AvailabilityDetail();
        detailDaniel5.setDate(startDate.plusDays(24)); // 25. Dezember
        detailDaniel5.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel5.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel6 = new AvailabilityDetail();
        detailDaniel6.setDate(startDate.plusDays(10)); // 11. Dezember
        detailDaniel6.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel6.setEntry(entryDaniel);

        AvailabilityDetail detailDaniel7 = new AvailabilityDetail();
        detailDaniel7.setDate(startDate.plusDays(28)); // 29. Dezember
        detailDaniel7.setStatus(AvailabilityStatus.UNAVAILABLE);
        detailDaniel7.setEntry(entryDaniel);



        entryDaniel.getDetails().addAll(List.of(detailDaniel1, detailDaniel2, detailDaniel3, detailDaniel4, detailDaniel5,
                detailDaniel6, detailDaniel7));
        availabilityEntryRepository.save(entryDaniel);


        System.out.println("Created " + availabilityEntryRepository.count() + " AvailabilityEntries for the schema.");


        System.out.println("--- Data Seeding Complete ---");
    }
}