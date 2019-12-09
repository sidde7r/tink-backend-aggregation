package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String value;
    private String currency;

    public Amount toTinkAmount() {

        return Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(currency)
                ? Amount.inEUR(0)
                : new Amount(currency, StringUtils.parseAmount(value));
    }
}
