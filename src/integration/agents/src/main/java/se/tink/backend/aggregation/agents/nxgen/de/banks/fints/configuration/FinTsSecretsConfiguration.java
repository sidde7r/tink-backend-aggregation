package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@Getter
@JsonObject
public class FinTsSecretsConfiguration implements ClientConfiguration {
    @JsonProperty @Secret private String productId;
    @JsonProperty @AgentConfigParam private String productVersion;

    public FinTsSecretsConfiguration() {}

    public FinTsSecretsConfiguration(String productId, String productVersion) {
        this.productId = productId;
        this.productVersion = productVersion;
    }
}
