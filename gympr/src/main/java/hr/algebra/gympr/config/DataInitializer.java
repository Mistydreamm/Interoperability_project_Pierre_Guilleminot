package hr.algebra.gympr.config;

import hr.algebra.gympr.entity.PersonalRecord;
import hr.algebra.gympr.entity.User;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import hr.algebra.gympr.enums.Role;
import hr.algebra.gympr.repository.PersonalRecordRepository;
import hr.algebra.gympr.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PersonalRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        UserRepository userRepository,
        PersonalRecordRepository recordRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@gympr.hr");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setEmail("user@gympr.hr");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        createRecord(LiftType.DEADLIFT, MuscleGroup.BACK,
            new BigDecimal("200.00"), 1, 1, 9,
            new BigDecimal("82.50"), "Fit Fabrika Zagreb", "Rogue Ohio Bar, straps, belt",
            LocalDate.of(2025, 4, 10), true,
            "Hit 200kg for the first time. Clean single, no mixed grip. Years in the making.", admin);

        createRecord(LiftType.SQUAT, MuscleGroup.LEGS,
            new BigDecimal("160.00"), 3, 1, 8,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Safety squat bar, belt, knee sleeves",
            LocalDate.of(2025, 4, 8), false,
            "3 clean reps at 160kg, last one was a grinder. Depth was good.", admin);

        createRecord(LiftType.BENCH_PRESS, MuscleGroup.CHEST,
            new BigDecimal("120.00"), 1, 1, 10,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Competition bench, wrist wraps",
            LocalDate.of(2025, 4, 6), true,
            "Finally broke 120kg bench. RPE 10, had to grind it. Pause rep.", admin);

        createRecord(LiftType.OVERHEAD_PRESS, MuscleGroup.SHOULDERS,
            new BigDecimal("70.00"), 1, 1, 9,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Standard barbell",
            LocalDate.of(2025, 4, 4), false,
            "Strict press, no leg drive. Great bar path, shoulders felt strong.", admin);

        createRecord(LiftType.PULL_UP, MuscleGroup.BACK,
            new BigDecimal("25.00"), 5, 3, 8,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Dip belt, 25kg plate",
            LocalDate.of(2025, 4, 3), false,
            "Weighted pull-ups 5x3 with 25kg. Pronated grip, full ROM.", admin);

        createRecord(LiftType.FRONT_SQUAT, MuscleGroup.LEGS,
            new BigDecimal("120.00"), 3, 1, 9,
            new BigDecimal("82.50"), "Fit Fabrika Zagreb", "Clean grip, wrist wraps",
            LocalDate.of(2025, 3, 30), false,
            "Upper back gave out on rep 3 but kept position. Elbow height was solid.", admin);

        createRecord(LiftType.BARBELL_ROW,  MuscleGroup.BACK,
            new BigDecimal("100.00"), 8, 4, 8,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Pendlay row style",
            LocalDate.of(2025, 3, 28), false,
            "Pendlay rows 4x8 at 100kg. Dead stop on floor between reps, strict form.", admin);

        createRecord(LiftType.HIP_THRUST, MuscleGroup.LEGS,
            new BigDecimal("180.00"), 5, 3, 8,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Hip thrust pad, barbell",
            LocalDate.of(2025, 3, 25), true,
            "180kg hip thrusts for 3x5. Glutes were cooked after.", admin);

        createRecord(LiftType.CLEAN_AND_JERK, MuscleGroup.FULL_BODY,
            new BigDecimal("90.00"), 1, 1, 9,
            new BigDecimal("82.00"), "CrossFit Zagreb", "Olympic bar, lifting shoes",
            LocalDate.of(2025, 3, 20), false,
            "Split jerk caught clean. Need to work on rack position, felt crashy.", admin);

        createRecord(LiftType.DEADLIFT, MuscleGroup.BACK,
            new BigDecimal("180.00"), 3, 1, 8,
            new BigDecimal("82.00"), "Fit Fabrika Zagreb", "Rogue Ohio Bar, belt",
            LocalDate.of(2025, 3, 15), false,
            "3x180kg sumo. Felt like speed work, set up for 200kg attempt.", admin);
    }

    private void createRecord(
        LiftType lift, MuscleGroup group,
        BigDecimal weight, Integer reps, Integer sets, Integer rpe,
        BigDecimal bodyweight, String gym, String equipment,
        LocalDate date, boolean milestone, String notes, User addedBy
    ) {
        PersonalRecord p = new PersonalRecord();
        p.setLiftType(lift);
        p.setMuscleGroup(group);
        p.setWeightKg(weight);
        p.setReps(reps);
        p.setSets(sets);
        p.setRpe(rpe);
        p.setBodyweightKg(bodyweight);
        p.setGymLocation(gym);
        p.setEquipment(equipment);
        p.setLiftDate(date);
        p.setMilestone(milestone);
        p.setNotes(notes);
        p.setAddedBy(addedBy);
        recordRepository.save(p);
    }
}
