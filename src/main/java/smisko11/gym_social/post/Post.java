package smisko11.gym_social.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import smisko11.gym_social.user.AppUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(name = "content", length = Integer.MAX_VALUE)   // TEXT u bazi
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // slike i video idu zajedno sa postom: sačuvaš post -> sačuvaju se i one, obrišeš post -> brišu se i one
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PostMedia> media = new ArrayList<>();

    // pomoćne metode drže OBE strane veze usklađenim
    public void addMedia(PostMedia item) {
        media.add(item);
        item.setPost(this);
    }

    public void removeMedia(PostMedia item) {
        media.remove(item);
        item.setPost(null);
    }
}