package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users_events")
public class UserEvent {
    protected Date date;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String remoteAddress;
    @Enumerated(EnumType.STRING)
    private UserEventTypes type;
    private String userId;

    public UserEvent() {
        date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public long getId() {
        return id;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public UserEventTypes getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setType(UserEventTypes type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("date", date).add("userId", userId).add("type", type)
                .toString();
    }
}
