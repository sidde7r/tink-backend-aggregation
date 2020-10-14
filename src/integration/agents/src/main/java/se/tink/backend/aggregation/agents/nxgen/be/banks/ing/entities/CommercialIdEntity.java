package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class CommercialIdEntity {

    private String type;
    private String value;
}
