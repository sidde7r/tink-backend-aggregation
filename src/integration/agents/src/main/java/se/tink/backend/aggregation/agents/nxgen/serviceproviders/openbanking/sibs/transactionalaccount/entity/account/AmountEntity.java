package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String currency;
    private String content;

    public Amount toTinkAmount() {
        return new Amount(currency, StringUtils.parseAmount(content));
    }
}
