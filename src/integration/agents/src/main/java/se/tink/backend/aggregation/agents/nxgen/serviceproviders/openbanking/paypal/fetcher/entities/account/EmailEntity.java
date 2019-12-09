package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmailEntity {
    private String value;
    private boolean primary;
    private boolean confirmed;

    public String getValue() {
        return value;
    }

    public boolean isConfirmedAndPrimary() {
        return confirmed && primary;
    }
}
