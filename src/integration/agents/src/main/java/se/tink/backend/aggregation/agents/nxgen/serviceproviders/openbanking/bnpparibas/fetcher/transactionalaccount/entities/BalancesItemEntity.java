package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.ResponseValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {

    private String balanceType;

    private String name;

    private String lastCommittedTransaction;

    @JsonProperty("balanceAmount")
    private AmountEntity amountEntity;

    public String getBalanceType() {
        return balanceType;
    }

    public String getName() {
        return name;
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    public boolean isAvailableBalance() {
        return ResponseValues.BALANCE_TYPE_OTHER.equalsIgnoreCase(balanceType)
                || ResponseValues.BALANCE_TYPE_CLOSING.equalsIgnoreCase(balanceType);
    }
}
