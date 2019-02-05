package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SigningIdHeaderDto extends HeaderDto {
    private TypeEncodedPair signingId;

    public TypeEncodedPair getSigningId() {
        return signingId;
    }
}
