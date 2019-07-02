package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public Amount getAmount() {
        return new Amount(
                Preconditions.checkNotNull(Strings.emptyToNull(balanceAmount.getCurrency())),
                StringUtils.parseAmount(
                        Preconditions.checkNotNull(
                                Strings.emptyToNull(balanceAmount.getAmount()))));
    }

    public boolean isExpected() {
        return balanceType.equalsIgnoreCase(VolvoFinansConstants.Accounts.STATUS_EXPECTED);
    }
}
