package smisko11.gym_social.schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import smisko11.gym_social.catalog.musclegroup.MuscleGroup;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "schedule_day")
public class ScheduleDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private WorkoutSchedule schedule;

    @Convert(converter = DayOfWeekConverter.class)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "note", length = 200)
    private String note;

    // čista vezna tabela (samo dva FK) -> @ManyToMany, bez posebnog entiteta
    @ManyToMany
    @JoinTable(
            name = "schedule_day_muscle_group",
            joinColumns = @JoinColumn(name = "schedule_day_id"),
            inverseJoinColumns = @JoinColumn(name = "muscle_group_id")
    )
    private Set<MuscleGroup> muscleGroups = new HashSet<>();   // prazno = dan odmora

    @OneToMany(mappedBy = "scheduleDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ScheduleDayExercise> exercises = new ArrayList<>();

    public void addExercise(ScheduleDayExercise exercise) {
        exercises.add(exercise);
        exercise.setScheduleDay(this);
    }

    public void removeExercise(ScheduleDayExercise exercise) {
        exercises.remove(exercise);
        exercise.setScheduleDay(null);
    }
}