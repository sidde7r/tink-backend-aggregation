package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    private String msgKey;
    private String message;

    public String getMsgKey() {
        return msgKey;
    }

    public String getMessage() {
        return message;
    }
}
