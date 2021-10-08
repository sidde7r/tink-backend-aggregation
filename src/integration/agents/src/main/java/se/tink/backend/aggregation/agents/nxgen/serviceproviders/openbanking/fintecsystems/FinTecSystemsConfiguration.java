package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class FinTecSystemsConfiguration implements ClientConfiguration {
    @SensitiveSecret private String apiKey;
}
