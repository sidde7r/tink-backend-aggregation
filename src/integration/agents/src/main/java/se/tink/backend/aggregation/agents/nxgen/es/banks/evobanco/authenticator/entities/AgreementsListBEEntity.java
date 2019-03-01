package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementsListBEEntity {
    @JsonProperty("ecvPersonaAcuerdo")
    private String ecvPersonAgreement;

    @JsonProperty("codigoTarifa")
    private String rateCode;

    @JsonProperty("usuario")
    private String user;

    @JsonProperty("acuerdoBE")
    private String agreementbe;
}
