package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NotificationFiltersEntity {
    @JsonProperty("notification_delivery_method")
    private String notificationDeliveryMethod;

    @JsonProperty("notification_target")
    private String notificationTarget;

    private String category;
}
