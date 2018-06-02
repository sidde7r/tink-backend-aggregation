package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.protostuff.Tag;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import org.xerial.snappy.Snappy;
import se.tink.backend.common.utils.LogUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.serialization.proto.utils.ProtoSerializationUtils;

@Table(value = "statistics")
public class CassandraStatistic implements Cloneable {
    private static final LogUtils log = new LogUtils(CassandraStatistic.class);

    @Tag(1)
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @Tag(2)
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private ResolutionTypes resolution;

    @Tag(3)
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private Integer periodHead;

    @Tag(4)
    @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private String type;

    @Tag(5)
    private ByteBuffer data;

    public CassandraStatistic() {
    }

    public CassandraStatistic(List<MinimizedStatistic> minimizedStatistics) {
        setStatistics(minimizedStatistics);
    }

    public Integer getPeriodHead() {
        return this.periodHead;
    }

    public ResolutionTypes getResolution() {
        return resolution;
    }

    public String getType() {
        return type;
    }

    public UUID getUserId() {
        return userId;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setPeriodHead(Integer periodHead) {
        this.periodHead = periodHead;
    }

    public void setResolution(ResolutionTypes resolution) {
        this.resolution = resolution;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public void setStatistics(List<MinimizedStatistic> minimizedStatistics) {
        byte[] compressed = null;

        try {
            compressed = Snappy
                    .compress(ProtoSerializationUtils.serializeToBinary(minimizedStatistics, MinimizedStatistic.class));
        } catch (IOException e) {
            log.error("Was not able to serialize and compress data", e);
        }

        this.data = ByteBuffer.wrap(compressed);
    }

    public List<MinimizedStatistic> getMinimizedStatistics() {
        if (this.data != null) {
            byte[] byteArray = this.data.array();
            List<MinimizedStatistic> minimizedStatistic = null;
            try {
                byte[] uncompressed = Snappy.uncompress(byteArray);
                minimizedStatistic = ProtoSerializationUtils
                        .deserializeFromBinary(uncompressed, MinimizedStatistic.class);
                return minimizedStatistic;
            } catch (IOException e) {
                log.error("Couldn't uncompress/read statistics data ", e);
                return ImmutableList.<MinimizedStatistic>of();
            }
        } else {
            return ImmutableList.<MinimizedStatistic>of();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CassandraStatistic that = (CassandraStatistic) o;

        if (!userId.equals(that.userId))
            return false;
        if (resolution != that.resolution)
            return false;
        if (!periodHead.equals(that.periodHead))
            return false;
        if (!type.equals(that.type))
            return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, periodHead, resolution, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("userId", userId).add("periodHead", periodHead)
                .add("resolution", resolution)
                .add("type", type)
                .toString();
    }

}
