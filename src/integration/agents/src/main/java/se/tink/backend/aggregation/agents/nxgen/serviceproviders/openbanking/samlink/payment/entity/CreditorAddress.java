package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddress {

    private String city;
    private String country;

    @JsonCreator
    public CreditorAddress(
            @JsonProperty("city") String city, @JsonProperty("country") String country) {
        this.city = city;
        this.country = country;
    }
}
