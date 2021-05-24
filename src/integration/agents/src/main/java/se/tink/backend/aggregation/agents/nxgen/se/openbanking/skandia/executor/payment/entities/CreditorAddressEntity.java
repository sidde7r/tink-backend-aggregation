package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddressEntity {
    private String city;
    private String country;
    private String street;
    private String buildingNumber;
    private String postalCode;

    public CreditorAddressEntity() {}

    public CreditorAddressEntity(String city, String country, String street) {
        this.city = city;
        this.country = country;
        this.street = street;
    }
}
