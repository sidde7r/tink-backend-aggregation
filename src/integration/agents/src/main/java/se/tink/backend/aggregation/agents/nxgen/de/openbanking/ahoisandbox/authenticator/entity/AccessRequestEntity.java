package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessRequestEntity {

    private String type;
    private String providerId;
    private AccessFieldsEntity accessFields;

    public AccessRequestEntity(String type, String providerId, AccessFieldsEntity accessFields) {
        this.type = type;
        this.providerId = providerId;
        this.accessFields = accessFields;
    }
}
