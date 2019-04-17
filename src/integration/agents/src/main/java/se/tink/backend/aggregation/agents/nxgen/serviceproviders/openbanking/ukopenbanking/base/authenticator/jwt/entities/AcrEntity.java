package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.entities;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AcrEntity {
    private boolean essential;
    private List<String> values;

    public AcrEntity(boolean essential, String... values) {
        this.essential = essential;
        this.values = Arrays.asList(values);
    }
}
