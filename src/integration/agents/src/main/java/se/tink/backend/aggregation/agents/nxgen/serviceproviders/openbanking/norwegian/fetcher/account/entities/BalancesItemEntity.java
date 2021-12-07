package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianConstants.ResponseValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class BalancesItemEntity {

    private String balanceType;

    @JsonProperty("balanceAmount")
    private AmountEntity amountEntity;

    boolean isExpectedBalance() {
        return ResponseValues.BALANCE_TYPE_EXPECTED.equalsIgnoreCase(balanceType);
    }

    boolean isClosingBalance() {
        return ResponseValues.BALANCE_TYPE_CLOSING.equalsIgnoreCase(balanceType);
    }

    boolean isAvailableBalance() {
        return ResponseValues.BALANCE_TYPE_AVAILABLE.equalsIgnoreCase(balanceType);
    }

    public boolean isCreditLimitIncluded() {
        return false;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return amountEntity.toAmount();
    }

    public Optional<BalanceType> getBalanceType() {
        return BalanceType.findByStringType(balanceType);
    }
}
