package com.example.OnePieceTheoryEvaluator.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class DailyVoteSeriesId implements Serializable {
  private Long theoryId;
  private LocalDate day;

  public DailyVoteSeriesId() {}
  public DailyVoteSeriesId(Long theoryId, LocalDate day) { this.theoryId = theoryId; this.day = day; }

  public Long getTheoryId() { return theoryId; }
  public void setTheoryId(Long theoryId) { this.theoryId = theoryId; }
  public LocalDate getDay() { return day; }
  public void setDay(LocalDate day) { this.day = day; }

  @Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof DailyVoteSeriesId that)) return false; return Objects.equals(theoryId, that.theoryId) && Objects.equals(day, that.day); }
  @Override public int hashCode(){ return Objects.hash(theoryId, day); }
}
