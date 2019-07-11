package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MifidInformationEntity {
    @JsonProperty("IsMifidCompliant")
    private boolean isMifidCompliant;

    @JsonProperty("Mifid")
    private boolean mifid;
}
