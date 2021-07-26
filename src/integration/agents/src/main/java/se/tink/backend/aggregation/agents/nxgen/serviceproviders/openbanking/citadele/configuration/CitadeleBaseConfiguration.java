package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
public class CitadeleBaseConfiguration implements ClientConfiguration {}
