package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class TransactionAmountEntity {
    private String currency;
    private BigDecimal amount;

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
