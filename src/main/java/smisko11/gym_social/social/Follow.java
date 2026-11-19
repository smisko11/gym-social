package smisko11.gym_social.social;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import smisko11.gym_social.user.AppUser;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "follow")
public class Follow {

    @EmbeddedId
    private FollowId id = new FollowId();

    @MapsId("followerId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private AppUser follower;   // ko prati

    @MapsId("followeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_id", nullable = false)
    private AppUser followee;   // koga prati

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}