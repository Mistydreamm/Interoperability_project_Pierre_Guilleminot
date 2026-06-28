package hr.algebra.project.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hr.algebra.project.frontend.enums.LiftType;
import hr.algebra.project.frontend.enums.MuscleGroup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalRecord {
    private Long id;
    private LiftType liftType;
    private MuscleGroup muscleGroup;
    private BigDecimal weightKg;
    private Integer reps;
    private Integer sets;
    private Integer rpe;
    private BigDecimal bodyweightKg;
    private String gymLocation;
    private String equipment;
    private LocalDate liftDate;
    private boolean milestone;
    private String notes;
    private String addedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public PersonalRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LiftType getLiftType() {
        return liftType;
    }

    public void setLiftType(LiftType liftType) {
        this.liftType = liftType;
    }

    public MuscleGroup getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(MuscleGroup muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getRpe() {
        return rpe;
    }

    public void setRpe(Integer rpe) {
        this.rpe = rpe;
    }

    public BigDecimal getBodyweightKg() {
        return bodyweightKg;
    }

    public void setBodyweightKg(BigDecimal bodyweightKg) {
        this.bodyweightKg = bodyweightKg;
    }

    public String getGymLocation() {
        return gymLocation;
    }

    public void setGymLocation(String gymLocation) {
        this.gymLocation = gymLocation;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public LocalDate getLiftDate() {
        return liftDate;
    }

    public void setLiftDate(LocalDate liftDate) {
        this.liftDate = liftDate;
    }

    public boolean isMilestone() {
        return milestone;
    }

    public void setMilestone(boolean milestone) {
        this.milestone = milestone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
