package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    private String country;
    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
}
