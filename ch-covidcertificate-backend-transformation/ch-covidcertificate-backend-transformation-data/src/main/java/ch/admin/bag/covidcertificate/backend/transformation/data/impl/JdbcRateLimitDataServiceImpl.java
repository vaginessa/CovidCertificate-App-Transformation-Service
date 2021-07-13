package ch.admin.bag.covidcertificate.backend.transformation.data.impl;

import ch.admin.bag.covidcertificate.backend.transformation.data.RateLimitDataService;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

public class JdbcRateLimitDataServiceImpl implements RateLimitDataService {

    private static final Logger logger =
            LoggerFactory.getLogger(JdbcRateLimitDataServiceImpl.class);

    private final NamedParameterJdbcTemplate jt;
    private final SimpleJdbcInsert rateLimitInsert;

    public JdbcRateLimitDataServiceImpl(DataSource dataSource) {
        this.jt = new NamedParameterJdbcTemplate(dataSource);
        this.rateLimitInsert =
                new SimpleJdbcInsert(dataSource)
                        .withTableName("t_rate_limit")
                        .usingGeneratedKeyColumns("pk_rate_limit", "created_at");
    }

    @Override
    @Transactional(readOnly = true)
    public int getCurrentRate(String uvciHash) {
        logger.debug("Fetching current count for uvci hash {}", uvciHash);
        final var getCountSql = "select count(1) from t_rate_limit where uvci_hash = :uvci_hash";
        final var params = new MapSqlParameterSource("uvci_hash", uvciHash);
        return jt.queryForObject(getCountSql, params, Integer.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void increaseRate(String uvciHash) {
        logger.debug("Adding entry for uvci hash: {}", uvciHash);
        if (uvciHash != null && !uvciHash.isBlank()) {
            var params = new MapSqlParameterSource("uvci_hash", uvciHash);
            rateLimitInsert.execute(params);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int cleanDb(Duration retentionPeriod) {
        var retentionTime = Date.from(Instant.now().minus(retentionPeriod));
        logger.debug("Removing entries before {}", retentionTime);
        var params = new MapSqlParameterSource("retention_time", retentionTime);
        var cleanupSql = "delete from t_rate_limit where created_at < :retention_time";
        return jt.update(cleanupSql, params);
    }
}
