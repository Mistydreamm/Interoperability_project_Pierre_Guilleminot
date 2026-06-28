package hr.algebra.gympr.repository;

import hr.algebra.gympr.entity.PersonalRecord;
import hr.algebra.gympr.enums.LiftType;
import hr.algebra.gympr.enums.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, Long> {

    @Query("""
        SELECT p FROM PersonalRecord p
        WHERE (:lift IS NULL OR p.liftType = :lift)
          AND (:group IS NULL OR p.muscleGroup = :group)
          AND (:milestone IS NULL OR p.milestone = :milestone)
          AND (:query IS NULL OR LOWER(p.gymLocation) LIKE LOWER(CONCAT('%', :query, '%'))
                              OR LOWER(p.equipment)   LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY p.liftDate DESC, p.createdAt DESC
        """)
    List<PersonalRecord> search(
        @Param("lift") LiftType lift,
        @Param("group") MuscleGroup group,
        @Param("milestone") Boolean milestone,
        @Param("query") String query
    );

    List<PersonalRecord> findAllByOrderByLiftDateDescCreatedAtDesc();
}
