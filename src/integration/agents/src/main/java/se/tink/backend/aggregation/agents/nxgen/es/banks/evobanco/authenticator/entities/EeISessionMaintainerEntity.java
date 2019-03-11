package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeISessionMaintainerEntity {
    @JsonProperty("usuarioBE")
    private String userBE;

    @JsonProperty("acuerdoBE")
    private String agreementBE;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    public EeISessionMaintainerEntity(String userBE, String agreementBE, String entityCode) {
        this.userBE = userBE;
        this.agreementBE = agreementBE;
        this.entityCode = entityCode;
    }
}
