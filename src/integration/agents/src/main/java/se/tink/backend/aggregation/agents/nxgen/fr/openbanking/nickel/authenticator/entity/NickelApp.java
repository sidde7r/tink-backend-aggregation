package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.entity;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@Data
public class NickelApp {
    private String type;
    private String version;
}
