package se.tink.backend.core.oauth2;

import io.protostuff.Exclude;
import io.protostuff.Tag;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "oauth2_authorizations_requests")
public class OAuth2AuthorizeRequest {
    @Id
    @JsonIgnore
    @Exclude
    private String code;
    @Tag(2)
    private String scope;
    @Tag(1)
    private String clientId;
    @Exclude
    @JsonIgnore
    private String userId;
    @Transient
    @Tag(3)
    private String redirectUri;

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public OAuth2AuthorizeRequest() {
        code = StringUtils.generateUUID();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}