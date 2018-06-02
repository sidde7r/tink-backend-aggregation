package se.tink.backend.rpc;

import java.util.Date;

public class DataExportRequest {

    private String id;
    private Date created;
    private DataExportRequestStatus status;

    public DataExportRequest() {
    }

    public DataExportRequest(String id, Date created, DataExportRequestStatus status) {
        this.id = id;
        this.created = created;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public DataExportRequestStatus getStatus() {
        return status;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setStatus(DataExportRequestStatus status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }
}
