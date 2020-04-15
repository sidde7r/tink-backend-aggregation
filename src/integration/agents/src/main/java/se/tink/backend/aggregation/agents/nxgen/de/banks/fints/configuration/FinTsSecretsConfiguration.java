package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FinTsSecretsConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String productId;
    @JsonProperty @AgentConfigParam private String productVersion;
}
