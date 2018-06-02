package se.tink.backend.core;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "data_export_fragments")
public class DataExportFragment {

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID id;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private int index;
    private ByteBuffer data;

    public DataExportFragment(UUID id, int index, ByteBuffer data) {
        this.id = id;
        this.index = index;
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public byte[] getByteData() {
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        return bytes;
    }
}
