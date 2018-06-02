package se.tink.backend.core;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users_session")
public class UserSession implements Serializable {
    private static final long serialVersionUID = -4000260744230467805L;

    @Id
    private String id;
    private String userId;
    private Date created;
    private Date expiry;
    @Enumerated(EnumType.STRING)
    private SessionTypes sessionType;
    private String oAuthClientId;

    public UserSession() {
        created = new Date();
    }

    public String getId() {
        return this.id;
    }

    public SessionTypes getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionTypes sessionType) {
        this.sessionType = sessionType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public String getOAuthClientId() {
        return oAuthClientId;
    }

    public void setOAuthClientId(String oauth2ClientId) {
        this.oAuthClientId = oauth2ClientId;
    }

}
