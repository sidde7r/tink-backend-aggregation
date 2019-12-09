package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddressEntity {

    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;
}
