package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EELinkingLoginAnswerEntity {
    @JsonProperty("referenciaOTP")
    private String referenceotp;

    public String getReferenceotp() {
        return referenceotp;
    }
}
