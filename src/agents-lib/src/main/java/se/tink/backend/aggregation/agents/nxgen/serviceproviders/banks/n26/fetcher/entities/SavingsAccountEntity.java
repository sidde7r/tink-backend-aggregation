package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsAccountEntity {
    private String id;
    private String name;
    private double monthlyAmount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMonthlyAmount() {
        return monthlyAmount;
    }
}
