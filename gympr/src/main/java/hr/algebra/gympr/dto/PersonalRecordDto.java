package hr.algebra.gympr.dto;

import hr.algebra.gympr.entity.PersonalRecord;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Personal record / PR data transfer object")
public record PersonalRecordDto(

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    Long id,

    @NotNull
    @Schema(description = "Lift type")
    LiftType liftType,

    @NotNull
    @Schema(description = "Primary muscle group")
    MuscleGroup muscleGroup,

    @NotNull @DecimalMin("0.0") @DecimalMax("500.0")
    @Schema(description = "Weight lifted in kg", example = "180.00")
    BigDecimal weightKg,

    @NotNull @Min(1) @Max(30)
    @Schema(description = "Number of reps", example = "1")
    Integer reps,

    @Min(1) @Max(20)
    @Schema(description = "Number of sets at this weight", example = "1")
    Integer sets,

    @Min(1) @Max(10)
    @Schema(description = "RPE (Rate of Perceived Exertion, 1–10)", example = "9")
    Integer rpe,

    @DecimalMin("0.0") @DecimalMax("500.0")
    @Schema(description = "Bodyweight at time of lift in kg", example = "82.5")
    BigDecimal bodyweightKg,

    @Size(max = 100)
    @Schema(description = "Gym location", example = "Fit Fabrika Zagreb")
    String gymLocation,

    @Size(max = 150)
    @Schema(description = "Equipment used", example = "Rogue Ohio Bar, chalk, belt")
    String equipment,

    @NotNull
    @Schema(description = "Date of the lift", example = "2025-04-10")
    LocalDate liftDate,

    @Schema(description = "Is this a milestone PR (e.g. 2 plates, 3 plates)")
    boolean milestone,

    @Size(max = 2000)
    @Schema(description = "Training notes, form cues, how it felt")
    String notes,

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    String addedBy,

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    LocalDateTime createdAt,

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    LocalDateTime updatedAt
) {
    public static PersonalRecordDto from(PersonalRecord p) {
        return new PersonalRecordDto(
            p.getId(),
            p.getLiftType(),
            p.getMuscleGroup(),
            p.getWeightKg(),
            p.getReps(),
            p.getSets(),
            p.getRpe(),
            p.getBodyweightKg(),
            p.getGymLocation(),
            p.getEquipment(),
            p.getLiftDate(),
            p.isMilestone(),
            p.getNotes(),
            p.getAddedBy() != null ? p.getAddedBy().getUsername() : null,
            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }

    public void applyTo(PersonalRecord p) {
        p.setLiftType(liftType);
        p.setMuscleGroup(muscleGroup);
        p.setWeightKg(weightKg);
        p.setReps(reps);
        p.setSets(sets);
        p.setRpe(rpe);
        p.setBodyweightKg(bodyweightKg);
        p.setGymLocation(gymLocation);
        p.setEquipment(equipment);
        p.setLiftDate(liftDate);
        p.setMilestone(milestone);
        p.setNotes(notes);
    }
}
