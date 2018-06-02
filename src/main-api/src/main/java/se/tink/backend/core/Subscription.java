package se.tink.backend.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "subscriptions")
@JsonIgnoreProperties(ignoreUnknown = true)
@IdClass(SubscriptionPk.class)
public class Subscription {
    @Id
    @Column(name="`type`")
    @Enumerated(EnumType.STRING)
    private SubscriptionType type;
    @Id
    @Column(name="`userid`")
    private String userId;
    
    private boolean subscribed;

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
    
}
