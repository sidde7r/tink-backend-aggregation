package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(StringUtils.parseAmount(amount), currency);
    }
}
