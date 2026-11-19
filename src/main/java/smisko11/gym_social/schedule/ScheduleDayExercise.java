package smisko11.gym_social.schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import smisko11.gym_social.catalog.exercise.Exercise;

@Getter
@Setter
@Entity
@Table(name = "schedule_day_exercise")
public class ScheduleDayExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_day_id", nullable = false)
    private ScheduleDay scheduleDay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "position", nullable = false)
    private Short position;

    @Column(name = "target_sets")
    private Short targetSets;        // npr. 4

    @Column(name = "target_reps_min")
    private Short targetRepsMin;     // npr. 8

    @Column(name = "target_reps_max")
    private Short targetRepsMax;     // npr. 10
}