package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.CodeCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.Pin1ScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaResponse {
    @JsonProperty("autorizacionSCA")
    private boolean autorizacionSca;

    @JsonProperty("pin1SCA")
    private Pin1ScaEntity pin1Sca;

    @JsonProperty("sms")
    private SmsEntity sms;

    @JsonProperty("tarjetaCoordenadas")
    private CodeCardEntity codeCard;

    @JsonProperty("tipoValidador")
    private String scaType;

    public String getScaType() {
        return scaType;
    }

    public Pin1ScaEntity getPin1Sca() {
        return pin1Sca;
    }

    public SmsEntity getSms() {
        return sms;
    }

    public CodeCardEntity getCodeCard() {
        return codeCard;
    }
}
