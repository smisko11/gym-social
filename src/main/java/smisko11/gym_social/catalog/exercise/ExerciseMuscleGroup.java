package smisko11.gym_social.catalog.exercise;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import smisko11.gym_social.catalog.musclegroup.MuscleGroup;

@Getter
@Setter
@Entity
@Table(name = "exercise_muscle_group")
public class ExerciseMuscleGroup {

    @EmbeddedId
    private ExerciseMuscleGroupId id = new ExerciseMuscleGroupId();

    @MapsId("exerciseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @MapsId("muscleGroupId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muscle_group_id", nullable = false)
    private MuscleGroup muscleGroup;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = true;
}