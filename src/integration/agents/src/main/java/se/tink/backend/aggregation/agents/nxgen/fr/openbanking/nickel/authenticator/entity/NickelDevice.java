package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator.entity;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@Data
public class NickelDevice {
    private String id;
    private String name;
    private String type;
}
