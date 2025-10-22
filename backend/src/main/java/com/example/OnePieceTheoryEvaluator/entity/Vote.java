package com.example.OnePieceTheoryEvaluator.entity;

import com.example.OnePieceTheoryEvaluator.enums.VoteType;
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
@Table(name = "votes")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VoteType type;
        
    private final LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theory_id")
    private Theory theory;

    @Override
    public String toString() {
        return "Vote{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
