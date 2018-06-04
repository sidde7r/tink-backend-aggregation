package se.tink.backend.core;

import java.util.UUID;

public class DataExport {

    private UUID userId;
    private UUID id;
    private int count;
    private int size;
    private String mimetype;

    public DataExport(UUID userId, UUID id, int count, int size, String mimetype) {
        this.userId = userId;
        this.id = id;
        this.count = count;
        this.size = size;
        this.mimetype = mimetype;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
}
