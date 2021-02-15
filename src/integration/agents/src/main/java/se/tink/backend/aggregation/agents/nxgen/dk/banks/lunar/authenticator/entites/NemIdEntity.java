package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.entites;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NemIdEntity {
    private final String signature;
    private final String challenge;
}
