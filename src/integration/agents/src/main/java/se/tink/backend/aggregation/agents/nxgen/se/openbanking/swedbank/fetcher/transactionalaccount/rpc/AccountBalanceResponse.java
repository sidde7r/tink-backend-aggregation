package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.function.Function;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AccountBalanceResponse {

    private List<AccountBalanceEntity> balances;

    public Amount getAvailableBalance(String defaultCurrency) {
        return balances.stream()
                .map(balance -> balance.getInterimAvailable().getAmount())
                .map(toTinkAmount(defaultCurrency))
                .findFirst()
                .orElse(new Amount(defaultCurrency, 0));
    }

    private Function<AmountEntity, Amount> toTinkAmount(String defaultCurrency) {
        return amount ->
                new Amount(
                        Strings.isNullOrEmpty(amount.getCurrency())
                                ? defaultCurrency
                                : amount.getCurrency(),
                        StringUtils.parseAmount(amount.getContent()));
    }
}
