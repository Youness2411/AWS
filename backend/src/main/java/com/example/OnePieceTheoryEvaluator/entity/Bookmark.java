package com.example.OnePieceTheoryEvaluator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "theory_id"})
})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theory_id", nullable = false)
    private Theory theory;

    @Column(name = "created_at", nullable = false)
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", theoryId=" + (theory != null ? theory.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
}

