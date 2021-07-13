package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AmountEntity {

    private String currency;
    private String amount;

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        StringUtils.parseAmount(amount);
        return ExactCurrencyAmount.of(amount, currency);
    }
}
