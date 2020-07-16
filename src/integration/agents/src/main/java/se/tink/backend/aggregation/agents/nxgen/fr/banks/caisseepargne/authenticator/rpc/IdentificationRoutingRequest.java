package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities.Characteristics;

@Builder
public class IdentificationRoutingRequest {

    @JsonProperty("characteristics")
    private Characteristics characteristics;
}
