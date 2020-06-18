package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class BpceGroupConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @Secret private String serverUrl;
    @Secret private String keyId;
}
