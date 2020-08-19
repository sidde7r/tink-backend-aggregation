package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BorrowerEntity {
    private String name;
    private String customerNumber;

    public String getName() {
        return name;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }
}
