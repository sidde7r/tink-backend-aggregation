package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessageEntity {
    private String messageId;

    public String getMessageId() {
        return messageId == null ? "" : messageId;
    }
}
