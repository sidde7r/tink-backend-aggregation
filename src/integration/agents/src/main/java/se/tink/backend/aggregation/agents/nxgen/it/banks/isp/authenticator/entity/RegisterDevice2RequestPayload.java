package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDevice2RequestPayload {

    @JsonProperty("ots")
    private String otpCode;

    @JsonProperty("trxots")
    private String transactionId;

    public RegisterDevice2RequestPayload(final String otpCode, final String transactionId) {
        this.otpCode = Objects.requireNonNull(otpCode);
        this.transactionId = Objects.requireNonNull(transactionId);
    }
}
