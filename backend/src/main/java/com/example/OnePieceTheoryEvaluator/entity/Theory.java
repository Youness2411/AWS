package com.example.OnePieceTheoryEvaluator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
// import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "theories")
public class Theory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    @Column(columnDefinition = "TEXT")
    private String content;

    // track current version number for convenience
    private Integer versionNumber;
    
    // AI evaluation score (0-100)
    private Integer aiScore;
    
    // Is this theory related to the last chapter?
    @Column(name = "is_related_to_last_chapter")
    @Builder.Default
    private Boolean isRelatedToLastChapter = false;
    
    // Is it necessary ? 
    // @OneToMany(mappedBy = "theory")
    // private List<Comment> comments;

    private String imageUrl;

    private LocalDateTime updatedAt;
    
    private final LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @Override
    public String toString() {
        return "Theory{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
