package smisko11.gym_social.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "exercise_set")
public class ExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_exercise_id", nullable = false)
    private SessionExercise sessionExercise;

    @Column(name = "set_number", nullable = false)
    private Short setNumber;             // 1., 2., 3. serija

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;         // null = bez tega; kod sopstvene težine = dodatni teret

    @Column(name = "reps", nullable = false)
    private Short reps;
}