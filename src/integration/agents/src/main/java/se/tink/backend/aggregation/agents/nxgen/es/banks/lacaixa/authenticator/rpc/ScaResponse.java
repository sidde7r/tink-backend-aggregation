package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.CodeCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.PinScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaResponse {
    @JsonProperty("autorizacionSCA")
    private boolean autorizacionSca;

    @JsonProperty("pin1SCA")
    private PinScaEntity pin1Sca;

    @JsonProperty("pin2Bankia")
    private PinScaEntity pin2ScaBankia;

    @JsonProperty("sms")
    private SmsEntity sms;

    @JsonProperty("tarjetaCoordenadas")
    private CodeCardEntity codeCard;

    @JsonProperty("tipoValidador")
    private String scaType;
}
