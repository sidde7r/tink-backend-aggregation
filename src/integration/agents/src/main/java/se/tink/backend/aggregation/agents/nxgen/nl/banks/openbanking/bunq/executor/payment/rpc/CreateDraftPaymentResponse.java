package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.entities.IdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateDraftPaymentResponse {
    @JsonProperty("Id")
    private IdEntity id;

    public long getId() {
        IdEntity idEntity =
                Optional.ofNullable(id)
                        .orElseThrow(() -> new IllegalStateException("Could not get Id."));

        return idEntity.getId();
    }
}
