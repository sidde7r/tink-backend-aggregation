package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SettingsEntity {
    @JsonProperty("QuickBalanceBalanceOnly")
    private boolean quickBalanceBalanceOnly;
    @JsonProperty("NotificationSubscription")
    private NotificationSubscriptionEntity notificationSubscription;

    public boolean isQuickBalanceBalanceOnly() {
        return quickBalanceBalanceOnly;
    }

    public NotificationSubscriptionEntity getNotificationSubscription() {
        return notificationSubscription;
    }
}
