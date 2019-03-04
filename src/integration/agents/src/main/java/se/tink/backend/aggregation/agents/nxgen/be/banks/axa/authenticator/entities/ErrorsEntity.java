package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class ErrorsEntity {
    private String msgCd;
    private String msgDetails;
    private String msgTypeCd;

    public String getMsgCd() {
        return msgCd;
    }

    public String getMsgDetails() {
        return msgDetails;
    }
}
