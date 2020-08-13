package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SignContentDataEntity {

    private String payload;
    private String signedPayload;

    public SignContentDataEntity(String payload, String signedPayload) {
        this.payload = payload;
        this.signedPayload = signedPayload;
    }
}
