package se.tink.backend.core;

import se.tink.backend.utils.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "users_advertiser_ids")
public class UserAdvertiserId {

    private Date created;
    @Id
    private String id;
    private String advertiserId;
    private boolean limitted;
    private Date updated;
    private String userId;
    private String deviceType;

    public UserAdvertiserId(String userId) {
        id = StringUtils.generateUUID();
        created = new Date();
        this.userId = userId;
    }

    public UserAdvertiserId() {
        id = StringUtils.generateUUID();
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isLimitted() {
        return limitted;
    }

    public void setLimitted(boolean limitted) {
        this.limitted = limitted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public void setAdvertiserId(String advertiserId) {
        this.advertiserId = advertiserId;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
