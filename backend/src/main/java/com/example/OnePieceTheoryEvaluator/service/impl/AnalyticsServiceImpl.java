package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeries;
import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeriesId;
import com.example.OnePieceTheoryEvaluator.repository.DailyVoteSeriesRepository;
import com.example.OnePieceTheoryEvaluator.repository.VoteAggregationRow;
import com.example.OnePieceTheoryEvaluator.repository.VoteRepository;
import com.example.OnePieceTheoryEvaluator.service.AnalyticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

  private final VoteRepository voteRepo;
  private final DailyVoteSeriesRepository seriesRepo;

  @PersistenceContext
  private EntityManager em;

  public AnalyticsServiceImpl(VoteRepository voteRepo, DailyVoteSeriesRepository seriesRepo) {
    this.voteRepo = voteRepo;
    this.seriesRepo = seriesRepo;
  }

  @Override
  @Transactional
  public void refreshDailySeries(LocalDate day) {
    List<VoteAggregationRow> rows = voteRepo.aggregateCountsByTheory();
    for (VoteAggregationRow r : rows) {
      int up = r.getUpCount() == null ? 0 : r.getUpCount();
      int down = r.getDownCount() == null ? 0 : r.getDownCount();
      int total = up + down;
      BigDecimal ratio = total > 0
          ? BigDecimal.valueOf(up * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
          : BigDecimal.ZERO;

      DailyVoteSeriesId id = new DailyVoteSeriesId(r.getTheoryId(), day);
      DailyVoteSeries s = seriesRepo.findById(id).orElseGet(() -> new DailyVoteSeries(id));
      s.setUpCount(up);
      s.setDownCount(down);
      s.setTotalCount(total);
      s.setUpRatio(ratio);
      seriesRepo.save(s);
    }
  }

  @Override
  public List<DailyVoteSeries> getSeries(Long theoryId, int days) {
    LocalDate since = LocalDate.now().minusDays(days - 1L);
    return seriesRepo.findByIdTheoryIdAndIdDayGreaterThanEqualOrderByIdDay(theoryId, since);
  }

    @Override
    @Transactional
    public void fakeBackfill(int days) {
    if (days < 1) days = 1;
    LocalDate start = LocalDate.now().minusDays(days - 1L);

    String sql = """
    WITH base AS (
        SELECT th.id AS theory_id,
            COALESCE(SUM(CASE WHEN v.type = 'UP' THEN 1 END),0) AS cur_up,
            COALESCE(SUM(CASE WHEN v.type = 'DOWN' THEN 1 END),0) AS cur_down,
            COALESCE(COUNT(v.id),0) AS cur_total
        FROM theories th
        LEFT JOIN votes v ON v.theory_id = th.id
        GROUP BY th.id
    ),
    days AS (
        SELECT d::date AS day, row_number() OVER (ORDER BY d) AS rn
        FROM generate_series(CAST(? AS date), current_date, interval '1 day') AS g(d)
    ),
    noise AS (
        SELECT day, rn,
            SUM((random() - 0.5) * 0.06) OVER (ORDER BY rn) AS drift
        FROM days
    ),
    series AS (
        SELECT
        b.theory_id,
        n.day,
        GREATEST(0.05, LEAST(0.95,
            (CASE WHEN b.cur_total > 0 THEN b.cur_up::double precision / b.cur_total ELSE 0.5 END) + n.drift
        )) AS ratio,
        GREATEST(5, ROUND((b.cur_total * (0.8 + random()*0.4))::numeric))::int AS total_count
        FROM base b
        CROSS JOIN noise n
    ),
    final AS (
        SELECT
        theory_id,
        day,
        ROUND((total_count * ratio)::numeric)::int AS up_count,
        (total_count - ROUND((total_count * ratio)::numeric)::int) AS down_count,
        total_count,
        ROUND((ratio * 100)::numeric, 2) AS up_ratio
        FROM series
    )
    INSERT INTO daily_vote_series
        (theory_id, day, up_count, down_count, total_count, up_ratio, created_at, updated_at)
    SELECT
        theory_id, day, up_count, down_count, total_count, up_ratio, now(), now()
    FROM final
    ON CONFLICT (theory_id, day) DO UPDATE
    SET up_count    = EXCLUDED.up_count,
        down_count  = EXCLUDED.down_count,
        total_count = EXCLUDED.total_count,
        up_ratio    = EXCLUDED.up_ratio,
        updated_at  = now();
    """;

    em.createNativeQuery(sql)
    .setParameter(1, java.sql.Date.valueOf(start))
    .executeUpdate();

}

}