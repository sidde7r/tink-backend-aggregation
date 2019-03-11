package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Detail {
    private List<SpecificAmountsItem> specificAmounts;

    public Amount getBalance() {
        return specificAmounts
                .stream()
                .filter(x -> x.getId().equalsIgnoreCase(BbvaMxConstants.VALUES.LOAN_BALANCE))
                .findFirst()
                .get()
                .getAmount()
                .getAmount();
    }
}
