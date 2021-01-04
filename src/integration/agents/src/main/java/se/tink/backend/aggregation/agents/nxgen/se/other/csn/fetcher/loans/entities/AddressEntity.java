package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {

    @JsonProperty("utdelningsadress")
    private String distributionAddress;

    @JsonProperty("isUtlandsk")
    private boolean isForeign;

    @JsonProperty("postnummerPostort")
    private String postalCode;
}
