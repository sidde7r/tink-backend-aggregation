package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentResponse {

    @JsonProperty("autorizacionSCA")
    private Boolean scaAuthorizationRequired;

    private ScaEntity pin1SCA;

    @JsonProperty("tipoValidador")
    private String validationType;

    private SmsEntity sms;

    @JsonProperty("estado")
    private String status;

    public Boolean getScaAuthorizationRequired() {
        return scaAuthorizationRequired;
    }

    public ScaEntity getPin1SCA() {
        return pin1SCA;
    }

    public String getValidationType() {
        return validationType;
    }

    public SmsEntity getSms() {
        return sms;
    }

    public String getStatus() {
        return status;
    }
}
