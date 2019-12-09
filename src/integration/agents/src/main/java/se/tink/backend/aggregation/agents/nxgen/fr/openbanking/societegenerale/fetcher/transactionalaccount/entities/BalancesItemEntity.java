package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {

    private String balanceType;

    private String name;

    private String lastChangeDateTime;

    private BalanceAmountEntity balanceAmount;

    private String referenceDate;

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }
}
