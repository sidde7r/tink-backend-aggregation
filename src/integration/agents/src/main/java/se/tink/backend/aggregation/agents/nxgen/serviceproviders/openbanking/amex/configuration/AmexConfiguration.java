package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration;

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
public class AmexConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;

    @SensitiveSecret @ClientSecretsConfiguration @UUIDConfiguration private String clientSecret;
}
