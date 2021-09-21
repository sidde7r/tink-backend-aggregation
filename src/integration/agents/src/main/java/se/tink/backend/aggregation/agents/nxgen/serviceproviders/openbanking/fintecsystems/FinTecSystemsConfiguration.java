package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;

@JsonObject
@Getter
public class FinTecSystemsConfiguration implements ClientConfiguration {
    @JsonProperty @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;
    @JsonProperty @ClientIdConfiguration @UUIDConfiguration private String redirectUrl;
    @JsonProperty @ClientIdConfiguration @UUIDConfiguration private String wizardUrl;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
}
