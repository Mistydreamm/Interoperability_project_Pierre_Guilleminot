package hr.algebra.gympr.entity;

import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "personal_records")
public class PersonalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiftType liftType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MuscleGroup muscleGroup;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("500.0")
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @NotNull
    @Min(1) @Max(30)
    @Column(nullable = false)
    private Integer reps;

    @Min(1) @Max(20)
    private Integer sets;

    @Min(1) @Max(10)
    private Integer rpe;

    @DecimalMin("0.0")
    @DecimalMax("500.0")
    @Column(precision = 6, scale = 2)
    private BigDecimal bodyweightKg;

    @Size(max = 100)
    private String gymLocation;

    @Size(max = 150)
    private String equipment;

    @NotNull
    @Column(nullable = false)
    private LocalDate liftDate;

    @Column(nullable = false)
    private boolean milestone = false;

    @Size(max = 2000)
    @Column(length = 2000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public LiftType getLiftType()                    { return liftType; }
    public void setLiftType(LiftType liftType)       { this.liftType = liftType; }
    public MuscleGroup getMuscleGroup()              { return muscleGroup; }
    public void setMuscleGroup(MuscleGroup m)        { this.muscleGroup = m; }
    public BigDecimal getWeightKg()                  { return weightKg; }
    public void setWeightKg(BigDecimal weightKg)     { this.weightKg = weightKg; }
    public Integer getReps()                         { return reps; }
    public void setReps(Integer reps)                { this.reps = reps; }
    public Integer getSets()                         { return sets; }
    public void setSets(Integer sets)                { this.sets = sets; }
    public Integer getRpe()                          { return rpe; }
    public void setRpe(Integer rpe)                  { this.rpe = rpe; }
    public BigDecimal getBodyweightKg()              { return bodyweightKg; }
    public void setBodyweightKg(BigDecimal b)        { this.bodyweightKg = b; }
    public String getGymLocation()                   { return gymLocation; }
    public void setGymLocation(String gymLocation)   { this.gymLocation = gymLocation; }
    public String getEquipment()                     { return equipment; }
    public void setEquipment(String equipment)       { this.equipment = equipment; }
    public LocalDate getLiftDate()                   { return liftDate; }
    public void setLiftDate(LocalDate liftDate)      { this.liftDate = liftDate; }
    public boolean isMilestone()                     { return milestone; }
    public void setMilestone(boolean milestone)      { this.milestone = milestone; }
    public String getNotes()                         { return notes; }
    public void setNotes(String notes)               { this.notes = notes; }
    public User getAddedBy()                         { return addedBy; }
    public void setAddedBy(User addedBy)             { this.addedBy = addedBy; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime t)        { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)        { this.updatedAt = t; }
}
