package smisko11.gym_social.workout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import smisko11.gym_social.catalog.gym.Gym;
import smisko11.gym_social.groupworkout.GroupWorkout;
import smisko11.gym_social.user.AppUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "workout_session")
public class WorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;                     // opciono

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_workout_id")
    private GroupWorkout groupWorkout;   // opciono: "radio sam sa Markom"

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "note", length = 500)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // vežbe pripadaju treningu: čuvaju se i brišu zajedno sa njim
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SessionExercise> exercises = new ArrayList<>();

    public void addExercise(SessionExercise exercise) {
        exercises.add(exercise);
        exercise.setSession(this);
    }

    public void removeExercise(SessionExercise exercise) {
        exercises.remove(exercise);
        exercise.setSession(null);
    }
}