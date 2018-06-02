package se.tink.backend.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "users_oauth2_client_roles")
@IdClass(UserOAuth2ClientPk.class)
public class UserOAuth2ClientRole {

    public static class Role {
        public static final String ADMIN = "ADMIN";
    }

    @Id
    @Column(name = "`clientid`")
    private String clientId;
    @Id
    @Column(name = "`userid`")
    private String userId;
    private String role;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
