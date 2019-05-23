package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities.UserPaymentServiceProviderEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionPSD2ProviderResponse {
    @JsonProperty("Id")
    private IdEntity id;

    @JsonProperty("Token")
    private TokenEntity token;

    @JsonProperty("UserPaymentServiceProvider")
    private UserPaymentServiceProviderEntity userPaymentServiceProvider;

    public IdEntity getId() {
        return Optional.ofNullable(id)
                .orElseThrow(() -> new IllegalStateException("Could not get Id."));
    }

    public TokenEntity getToken() {
        return token;
    }

    public UserPaymentServiceProviderEntity getUserPaymentServiceProvider() {
        return userPaymentServiceProvider;
    }
}
