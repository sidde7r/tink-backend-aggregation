package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddressEntity {

    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;

    public CreditorAddressEntity() {}

    public CreditorAddressEntity(
            String street, String buildingNumber, String city, String postalCode, String country) {
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }
}
