package se.tink.backend.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.xerial.snappy.Snappy;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.LogUtils;

@Entity
@Table(name = "statistics")
public class StatisticContainer {
    private static final LogUtils log = new LogUtils(StatisticContainer.class);

    @SuppressWarnings("serial")
    private static class StatisticList extends ArrayList<Statistic> {
        @SuppressWarnings("unused")
        public StatisticList() {

        }
    }

    public StatisticContainer() {

    }

    public StatisticContainer(byte[] data) {
        this.data = data;
    }

    public StatisticContainer(List<Statistic> statistics) {
        setStatistics(statistics);
    }

    @Lob
    @Column(length = 1048576)
    private byte[] data;

    @Id
    private String userId;

    public byte[] getData() {
        return data;
    }

    public String getUserId() {
        return userId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatistics(List<Statistic> statistics) {
        try {
            this.data = Snappy.compress(SerializationUtils.serializeToBinary(statistics));
        } catch (IOException e) {
            log.error("Was not able to serialize and compress data", e);
        }
    }

    public List<Statistic> getStatistics() {
        if (data == null) {
            return null;
        }

        try {
            return SerializationUtils.deserializeFromBinary(Snappy.uncompress(data), StatisticList.class);
        } catch (IOException e) {
            log.error("Was not able to uncompress and deserialize data", e);
            return null;
        }
    }
}
