package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NotificationSubscriptionEntity {
    @JsonProperty("MessageTypes")
    private List<MessageTypeEntity> messageTypes;

    public List<MessageTypeEntity> getMessageTypes() {
        return messageTypes;
    }
}
