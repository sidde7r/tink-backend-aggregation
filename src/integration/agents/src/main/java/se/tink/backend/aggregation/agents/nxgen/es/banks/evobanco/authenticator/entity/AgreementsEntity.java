package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementsEntity {
    @JsonProperty("acuerdoBE")
    private String agreementbe;

    @JsonProperty("codigoTarifa")
    private String rateCode;

    @JsonProperty("ecvPersonaAcuerdo")
    private String ecvPersonAgreement;

    @JsonProperty("usuario")
    private String user;
}
