package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
@Setter
public class BpceGroupConfiguration implements ClientConfiguration {

    @JsonIgnore private String clientId;
    @JsonIgnore private String serverUrl;
    @Secret private String keyId;
}
