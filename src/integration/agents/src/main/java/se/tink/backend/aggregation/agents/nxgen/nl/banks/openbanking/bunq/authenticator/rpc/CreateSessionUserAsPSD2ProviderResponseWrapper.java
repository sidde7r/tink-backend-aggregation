package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;

public class CreateSessionUserAsPSD2ProviderResponseWrapper {

    @Getter
    @JsonProperty("Response")
    private BunqResponse<CreateSessionUserAsPSD2ProviderResponse> response;
}
