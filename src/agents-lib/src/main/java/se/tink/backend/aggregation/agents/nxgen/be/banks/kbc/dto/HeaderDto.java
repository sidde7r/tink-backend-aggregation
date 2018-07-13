package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HeaderDto {
    private TypeValuePair resultCode;
    private TypeValuePair resultMessage;

    public TypeValuePair getResultCode() {
        return resultCode;
    }

    public TypeValuePair getResultMessage() {
        return resultMessage;
    }
}
