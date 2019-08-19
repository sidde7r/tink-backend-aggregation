package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddress {

    private String street;
    private String buildingNumber;
    private String city;
    private String postalCode;
    private String country;
}
