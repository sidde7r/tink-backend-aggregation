package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicerEntity {

    @JsonProperty("Identification")
    private String identification;

    @JsonProperty("SchemeName")
    private String schemeName;
}
