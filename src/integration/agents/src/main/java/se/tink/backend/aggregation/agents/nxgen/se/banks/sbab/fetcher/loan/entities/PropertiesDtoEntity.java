package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesDtoEntity extends StandardResponse {
    private List<PropertiesEntity> properties;

    public List<PropertiesEntity> getProperties() {
        return properties;
    }
}
