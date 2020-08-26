package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.UUIDConfiguration;

@JsonObject
@Getter
public class RedsysConfiguration implements ClientConfiguration {
    @Secret @ClientIdConfiguration @UUIDConfiguration private String clientId;
}
