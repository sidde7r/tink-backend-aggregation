package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupHeaderEntity {
    private String messageIdentification;
    private String creationDateTime;
    private int httpCode;

    public String getMessageIdentification() {
        return messageIdentification;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
