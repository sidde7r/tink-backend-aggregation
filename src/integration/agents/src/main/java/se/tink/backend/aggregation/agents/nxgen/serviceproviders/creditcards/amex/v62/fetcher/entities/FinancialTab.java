package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialTab {

    private SummaryFieldEntity totalBalance;

    public SummaryFieldEntity getTotalBalance() {
        return totalBalance;
    }
}
