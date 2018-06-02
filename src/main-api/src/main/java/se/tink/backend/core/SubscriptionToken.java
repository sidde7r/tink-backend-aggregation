package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "subscription_tokens")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscriptionToken {
    @Id
    private String token;

    private String userId;

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
