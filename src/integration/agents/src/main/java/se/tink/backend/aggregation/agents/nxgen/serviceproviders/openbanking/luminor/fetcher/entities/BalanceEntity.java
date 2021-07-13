package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMappable;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class BalanceEntity implements BalanceMappable {

    @JsonProperty("balanceAmount")
    BalanceAmountEntity balanceAmountEntity;

    String balanceType;
    boolean creditLimitIncluded;

    public boolean isCreditLimitIncluded() {
        return creditLimitIncluded;
    }

    @Override
    public ExactCurrencyAmount toTinkAmount() {
        if (balanceAmountEntity.getAmount() == null) {
            throw new IllegalStateException("Balance amount is not available");
        }
        return ExactCurrencyAmount.of(
                Double.parseDouble(balanceAmountEntity.getAmount()),
                balanceAmountEntity.getCurrency());
    }

    @Override
    public Optional<BalanceType> getBalanceType() {
        return BalanceType.findByStringType(balanceType);
    }
}
