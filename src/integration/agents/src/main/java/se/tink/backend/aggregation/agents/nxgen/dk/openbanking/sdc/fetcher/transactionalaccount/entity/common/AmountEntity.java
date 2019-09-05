package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {

    private String currency;
    private String amount;

    public Amount toAmount() {
        return new Amount(
                Preconditions.checkNotNull(Strings.emptyToNull(currency)),
                StringUtils.parseAmount(amount));
    }
}
