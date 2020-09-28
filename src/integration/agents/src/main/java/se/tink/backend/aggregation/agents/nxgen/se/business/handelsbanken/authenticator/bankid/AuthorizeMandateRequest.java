package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeMandateRequest {
    @JsonProperty("agreementNmbrs")
    private List<String> agreementNumbers;

    public AuthorizeMandateRequest setAgreementNumbers(String agreementNumber) {
        this.agreementNumbers = Collections.singletonList(agreementNumber);
        return this;
    }
}
