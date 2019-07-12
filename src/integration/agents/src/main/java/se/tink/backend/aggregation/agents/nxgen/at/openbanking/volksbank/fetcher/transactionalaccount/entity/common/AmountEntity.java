package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String currency;
    private String amount;

    public Amount toTinkAmount() {
        return new Amount(currency, StringUtils.parseAmount(amount));
    }
}
