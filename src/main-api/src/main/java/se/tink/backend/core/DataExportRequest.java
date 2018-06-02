package se.tink.backend.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import se.tink.backend.rpc.DataExportRequestStatus;
import se.tink.libraries.uuid.UUIDUtils;

@Entity
@Table(name = "data_export_requests")
public class DataExportRequest {

    @Id
    private String id;
    @NotNull
    private String userId;
    @NotNull
    private Date created;
    private Date updated;
    @NotNull
    @Enumerated(EnumType.STRING)
    private DataExportRequestStatus status;
    @Deprecated
    private String link;
    private String hash;
    private String salt;
    private String dataExportId; // should be used instead of link (better naming)

    public DataExportRequest() {
        this.id = UUIDUtils.generateUUID();
        this.status = DataExportRequestStatus.CREATED;
        this.created = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        Preconditions.checkNotNull(created);
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        this.id = id;
    }

    public DataExportRequestStatus getStatus() {
        return status;
    }

    public void setStatus(DataExportRequestStatus status) {
        Preconditions.checkNotNull(status);
        this.status = status;
    }

    @Deprecated
    public String getLink() {
        return link;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setDataExportId(String dataExportId) {
        this.dataExportId = dataExportId;
    }

    public String getDataExportId() {
        return dataExportId;
    }
}
