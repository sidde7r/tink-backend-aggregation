package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.rpc.account;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v30.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountsV30Response extends BaseResponse<List<AccountEntity>> {

    private Stream<AccountEntity> stream() {
        return getData().stream();
    }

    public static List<TransactionalAccount> toTransactionalAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balances) {

        Map<String, AccountBalanceEntity> accountBalances = balances.toMap();
        return accounts.stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CHECKING))
                .filter(e -> accountBalances.containsKey(e.getAccountId()))
                .map(e -> AccountEntity.toTransactionalAccount(e, accountBalances.get(e.getAccountId())))
                .collect(Collectors.toList());
    }

    public static List<CreditCardAccount> toCreditCardAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balances) {

        Map<String, AccountBalanceEntity> accountBalances = balances.toMap();
        return accounts.stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CREDIT_CARD))
                .filter(e -> accountBalances.containsKey(e.getAccountId()))
                .map(e -> AccountEntity.toCreditCardAccount(e, accountBalances.get(e.getAccountId())))
                .collect(Collectors.toList());
    }
}
