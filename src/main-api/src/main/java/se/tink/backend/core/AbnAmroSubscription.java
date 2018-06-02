package se.tink.backend.core;

import io.protostuff.Tag;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import se.tink.backend.utils.StringUtils;

/**
 * Model a subscription for ABN AMRO.
 *
 * - A subscription is active if the customer has accepted the terms & conditions.
 * - Subscription Id is the id of the subscription at ABN AMRO
 *
 */
@Entity
@Table(name = "abnamro_subscriptions")
public class AbnAmroSubscription {

    @Id
    @Tag(1)
    private String id;

    @Tag(2)
    private String userId;

    @Tag(3)
    private Long subscriptionId;

    @Tag(4)
    private Date activationDate;

    public AbnAmroSubscription() {
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

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public boolean isActivated() {
        return activationDate != null;
    }
}
