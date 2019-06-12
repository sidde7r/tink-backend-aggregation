package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;

    public void setCountry(String country) {
        this.country = country;
    }
}
