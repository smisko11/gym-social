package smisko11.gym_social.catalog.exercise;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class ExerciseMuscleGroupId implements Serializable {

    @Serial
    private static final long serialVersionUID = -482797906444368113L;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "muscle_group_id", nullable = false)
    private Long muscleGroupId;
}