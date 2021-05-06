package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class LocationEntity {
    private final boolean enabled;
}
