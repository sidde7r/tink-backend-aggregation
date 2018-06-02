package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

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
