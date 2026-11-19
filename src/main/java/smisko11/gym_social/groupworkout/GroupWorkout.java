package smisko11.gym_social.groupworkout;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import smisko11.gym_social.catalog.gym.Gym;
import smisko11.gym_social.catalog.musclegroup.MuscleGroup;
import smisko11.gym_social.user.AppUser;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "group_workout")
public class GroupWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private AppUser organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @Column(name = "planned_start", nullable = false)
    private Instant plannedStart;        // "idem u 18h"

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "max_participants")
    private Short maxParticipants;       // null = bez ograničenja

    @Column(name = "description", length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GroupWorkoutStatus status = GroupWorkoutStatus.SCHEDULED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // koji deo tela se radi; čista vezna tabela -> @ManyToMany
    @ManyToMany
    @JoinTable(
            name = "group_workout_muscle_group",
            joinColumns = @JoinColumn(name = "group_workout_id"),
            inverseJoinColumns = @JoinColumn(name = "muscle_group_id")
    )
    private Set<MuscleGroup> muscleGroups = new HashSet<>();
}