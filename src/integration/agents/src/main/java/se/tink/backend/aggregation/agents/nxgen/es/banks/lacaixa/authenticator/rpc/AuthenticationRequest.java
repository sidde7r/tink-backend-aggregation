package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationRequest {
    @JsonProperty("codigo")
    private List<String> code;

    @JsonProperty("adaptadoOTPSMS")
    private boolean adaptedOtpSms = true;

    public AuthenticationRequest(String code) {
        this.code = ImmutableList.of(code);
    }
}
