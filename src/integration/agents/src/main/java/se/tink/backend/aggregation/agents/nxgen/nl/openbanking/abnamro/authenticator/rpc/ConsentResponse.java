package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    @JsonProperty("iban")
    private String accountId;

    private String transactionId;

    private String scopes;

    @JsonProperty("valid")
    private Long valid;

    public boolean isValid() {
        return valid.compareTo(Instant.now().getEpochSecond()) > 0;
    }
}
