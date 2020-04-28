package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String currency;
    private String amount;

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(
                StringUtils.parseAmount(amount),
                Preconditions.checkNotNull(Strings.emptyToNull(currency)));
    }
}
