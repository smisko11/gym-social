package smisko11.gym_social.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import smisko11.gym_social.catalog.exercise.Exercise;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "session_exercise")
public class SessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "position", nullable = false)
    private Short position;              // redosled vežbi: 1, 2, 3...

    // serije pripadaju vežbi: čuvaju se i brišu zajedno sa njom
    @OneToMany(mappedBy = "sessionExercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<ExerciseSet> sets = new ArrayList<>();

    public void addSet(ExerciseSet set) {
        sets.add(set);
        set.setSessionExercise(this);
    }

    public void removeSet(ExerciseSet set) {
        sets.remove(set);
        set.setSessionExercise(null);
    }
}