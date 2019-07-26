package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.BALANCE_TYPE;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesItemEntity implements Comparable<BalancesItemEntity> {

    @JsonProperty("balanceType")
    private String balanceType;

    @JsonProperty("lastChangeDateTime")
    private String lastChangeDateTime;

    @JsonProperty("balanceAmount")
    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount getBalanceAmount() {
        return balanceAmount.getAmount();
    }

    public void setCurrencyIfNull(String currency) {
        balanceAmount.setCurrecyIfNull(currency);
    }

    protected BALANCE_TYPE getBalanceType() {
        return Optional.ofNullable(BALANCE_TYPE.fromString(balanceType))
                .orElseThrow(() -> new IllegalStateException("No balance type found"));
    }

    @Override
    public int compareTo(BalancesItemEntity other) {
        return getBalanceType().compareTo(other.getBalanceType());
    }
}
