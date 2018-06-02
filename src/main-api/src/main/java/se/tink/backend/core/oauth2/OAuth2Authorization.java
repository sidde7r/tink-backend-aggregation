package se.tink.backend.core.oauth2;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "oauth2_authorizations")
public class OAuth2Authorization implements Cloneable {
    public static final long ACCESS_TOKEN_TIMEOUT = 7200 * 1000;

    @Id
    private String id;
    @JsonIgnore
    private String userId;
    private String scope;
    private String accessToken;
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @JsonIgnore
    private Date updated;
    @JsonIgnore
    private Date created;
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public OAuth2Authorization() {
        id = StringUtils.generateUUID();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void refreshToken() {
        refreshToken = StringUtils.generateUUID();
        accessToken = StringUtils.generateUUID();
        updated = new Date();
    }
}
