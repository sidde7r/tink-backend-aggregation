package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private String creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(
                BnpParibasBaseConstants.ResponseValues.AVAILABLE_BALANCE);
    }

    public ExactCurrencyAmount toAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(b -> new ExactCurrencyAmount(b.getAmount(), b.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Could not parse amount"));
    }
}
