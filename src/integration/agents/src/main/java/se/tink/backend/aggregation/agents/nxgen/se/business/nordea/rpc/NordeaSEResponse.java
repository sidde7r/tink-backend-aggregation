package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaSEResponse {
    @JsonProperty("bankingServiceResponse")
    private BankingServiceResponse bankingServiceResponse;

    public Optional<String> getErrorCode() {
        return Optional.ofNullable(bankingServiceResponse)
                .map(BankingServiceResponse::getErrorCode);
    }
}
