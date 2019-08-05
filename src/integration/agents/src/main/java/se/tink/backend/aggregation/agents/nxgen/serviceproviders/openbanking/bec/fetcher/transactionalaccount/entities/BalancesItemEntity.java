package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesItemEntity implements Comparable<BalancesItemEntity> {

    @JsonProperty("balanceType")
    private String BalanceType;

    private String lastChangeDateTime;

    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount getBalanceAmount() {
        return balanceAmount.getAmount();
    }

    public void setCurrencyIfNull(String currency) {
        balanceAmount.setCurrecyIfNull(currency);
    }

    protected BecConstants.BalanceType getBalanceType() {
        return Optional.ofNullable(BecConstants.BalanceType.fromString(BalanceType))
                .orElseThrow(() -> new IllegalStateException("No balance type found"));
    }

    @Override
    public int compareTo(BalancesItemEntity other) {
        return getBalanceType().compareTo(other.getBalanceType());
    }
}
