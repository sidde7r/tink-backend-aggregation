package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OwnerEntity {
    private String name;
    private String customerNumber;
    private String customernumber;

    public String getName() {
        return name;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public String getCustomernumber() {
        return customernumber;
    }
}
