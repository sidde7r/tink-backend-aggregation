package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResourcesEntity {
    private String resourceId;

    @JsonIgnore
    public String getResourceId() {
        return resourceId;
    }
}
