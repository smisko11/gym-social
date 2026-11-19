package smisko11.gym_social.user.profile;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import smisko11.gym_social.catalog.gym.Gym;
import smisko11.gym_social.user.AppUser;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "display_name", length = 60)
    private String displayName;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "city", length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal", length = 20)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 20)
    private ExperienceLevel experienceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_gym_id")
    private Gym homeGym;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ProfileVisibility visibility = ProfileVisibility.PUBLIC;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}