package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.upcomingtransaction;

import se.tink.backend.aggregation.annotations.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonObject
public class CreditorAccountEntity {
    @JsonProperty("SchemeName")
    private String schemeName;
    @JsonProperty("Identification")
    private String identification;
    @JsonProperty("Name")
    private String name;
}
