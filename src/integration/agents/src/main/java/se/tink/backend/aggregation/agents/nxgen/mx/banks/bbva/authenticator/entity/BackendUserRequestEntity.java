package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BackendUserRequestEntity {
    private String accessCode;
    private String dialogId;
    private String userId;

    public BackendUserRequestEntity(String phonenumber) {
        this.accessCode = phonenumber;
        this.dialogId = "";
        this.userId = "";
    }
}
