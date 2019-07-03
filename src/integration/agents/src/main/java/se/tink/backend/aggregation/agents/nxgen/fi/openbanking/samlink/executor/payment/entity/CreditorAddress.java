package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAddress {

    private String city;
    private String country;

    public CreditorAddress() {}

    public CreditorAddress(String city, String country) {
        this.city = city;
        this.country = country;
    }
}
