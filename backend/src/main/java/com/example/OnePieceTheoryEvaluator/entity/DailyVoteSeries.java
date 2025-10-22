package com.example.OnePieceTheoryEvaluator.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_vote_series")
public class DailyVoteSeries {
  @EmbeddedId
  private DailyVoteSeriesId id;

  @Column(name = "up_count", nullable = false)
  private int upCount;

  @Column(name = "down_count", nullable = false)
  private int downCount;

  @Column(name = "total_count", nullable = false)
  private int totalCount;

  @Column(name = "up_ratio", nullable = false, precision = 5, scale = 2)
  private BigDecimal upRatio;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public DailyVoteSeries() {}
  public DailyVoteSeries(DailyVoteSeriesId id){ this.id = id; }

  @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }
  @PrePersist public void onPersist(){ this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt; }

  public DailyVoteSeriesId getId(){ return id; }
  public void setId(DailyVoteSeriesId id){ this.id = id; }
  public int getUpCount(){ return upCount; }
  public void setUpCount(int upCount){ this.upCount = upCount; }
  public int getDownCount(){ return downCount; }
  public void setDownCount(int downCount){ this.downCount = downCount; }
  public int getTotalCount(){ return totalCount; }
  public void setTotalCount(int totalCount){ this.totalCount = totalCount; }
  public BigDecimal getUpRatio(){ return upRatio; }
  public void setUpRatio(BigDecimal upRatio){ this.upRatio = upRatio; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt(){ return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
