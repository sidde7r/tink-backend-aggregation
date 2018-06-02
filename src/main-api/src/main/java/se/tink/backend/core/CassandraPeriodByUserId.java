package se.tink.backend.core;

import java.util.Comparator;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Transactions indexed by userid and period
 */
@Table(value = "period_by_userid")
public class CassandraPeriodByUserId implements Cloneable, Comparable<CassandraPeriodByUserId> {
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private int period;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getPeriod() { return period; }

    public void setPeriod(int period) { this.period = period; }

    @Override
    public int compareTo(CassandraPeriodByUserId o) {
        return Comparator.comparing(CassandraPeriodByUserId::getUserId)
                .thenComparingInt(CassandraPeriodByUserId::getPeriod)
                .compare(this, o);
    }
}
