package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    @JsonProperty("address_line1")
    private String addressLine1;

    private String city;

    @JsonProperty("country_code")
    private String countryCode;
}
