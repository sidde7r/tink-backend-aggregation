package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class LinkEntity {

    private String rel;
    private String href;
}
