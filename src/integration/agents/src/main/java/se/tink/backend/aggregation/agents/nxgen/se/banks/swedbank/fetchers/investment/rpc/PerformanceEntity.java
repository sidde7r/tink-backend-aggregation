package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PerformanceEntity {
    private String percent;
    private AmountEntity amount;

    public String getPercent() {
        return percent;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    @JsonIgnore
    public Optional<ExactCurrencyAmount> getTinkAmount(String defaultCurrency) {
        return Optional.ofNullable(amount)
                .map(amountEntity -> amountEntity.toTinkAmount(defaultCurrency));
    }
}
