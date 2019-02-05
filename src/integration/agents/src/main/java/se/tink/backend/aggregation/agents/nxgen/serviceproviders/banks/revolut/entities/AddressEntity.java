package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    private String city;
    private String country;
    private String postcode;
    private String region;
    private String streetLine1;
    private String streetLine2;

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getRegion() {
        return region;
    }

    public String getStreetLine1() {
        return streetLine1;
    }

    public String getStreetLine2() {
        return streetLine2;
    }
}
