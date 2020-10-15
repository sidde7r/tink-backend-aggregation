package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
@AllArgsConstructor
public class ItemEntity {
    private String type;
    private String text;
}
