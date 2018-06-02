package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignTypeDto {
    private TypeValuePair signType;
    private TypeEncodedPair signTypeId;

    public TypeValuePair getSignType() {
        return signType;
    }

    public TypeEncodedPair getSignTypeId() {
        return signTypeId;
    }
}
