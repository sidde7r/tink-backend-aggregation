package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBalanceEntity {

    private CardBalanceAmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String lastCommittedTransaction;
    private String referenceDate;

    public CardBalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }
}
