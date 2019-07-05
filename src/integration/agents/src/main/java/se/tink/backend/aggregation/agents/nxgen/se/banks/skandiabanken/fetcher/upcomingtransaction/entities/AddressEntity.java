package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class AddressEntity {
    @JsonProperty("CareOf")
    private String careOf;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("PostalAddress")
    private String postalAddress;

    @JsonProperty("StreetAddress")
    private String streetAddress;
}
