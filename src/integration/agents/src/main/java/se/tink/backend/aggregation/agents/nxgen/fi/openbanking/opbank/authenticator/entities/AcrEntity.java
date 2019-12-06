package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AcrEntity {
    private boolean essential;
    private List<String> values;

    public AcrEntity(boolean essential, List<String> values) {
        this.essential = essential;
        this.values = values;
    }
}
