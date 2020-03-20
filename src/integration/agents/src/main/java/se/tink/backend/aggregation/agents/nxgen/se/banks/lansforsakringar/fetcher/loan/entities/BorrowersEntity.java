package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BorrowersEntity {
    private String name;
    private String debtOwnershipShare;
    private String interestRateOwnershipShare;

    public String getName() {
        return name;
    }

    public String getDebtOwnershipShare() {
        return debtOwnershipShare;
    }

    public String getInterestRateOwnershipShare() {
        return interestRateOwnershipShare;
    }

    @Override
    public String toString() {
        return name;
    }
}
