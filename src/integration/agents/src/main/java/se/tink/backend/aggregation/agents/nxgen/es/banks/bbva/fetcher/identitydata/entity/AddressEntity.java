package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    private String streetName;
    private int streetNumber;
    private String city;
    private String zipCode;
    private String additionalInformation;
    private String state;
    private String stateCode;

    public String getStreetName() {
        return streetName;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public String getState() {
        return state;
    }

    public String getStateCode() {
        return stateCode;
    }
}
