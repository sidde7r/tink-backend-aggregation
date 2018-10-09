package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.rpc.account;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountsV11Response extends BaseResponse<List<AccountEntity>> {

    public static List<TransactionalAccount> toTransactionalAccount(AccountsV11Response accounts,
            AccountBalanceV11Response balances) {

        Map<String, AccountBalanceEntity> accountBalances = balances.toMap();

        return accounts.getData().stream()
                .filter(e -> e.getAccountType() == AccountTypes.CHECKING)
                .filter(e -> accountBalances.containsKey(e.getAccountId()))
                .map(e -> AccountEntity.toTransactionalAccount(e, accountBalances.get(e.getAccountId())))
                .collect(Collectors.toList());
    }

    public static List<CreditCardAccount> toCreditCardlAccount(AccountsV11Response accounts,
            AccountBalanceV11Response balances) {

        Map<String, AccountBalanceEntity> accountBalances = balances.toMap();

        return accounts.getData().stream()
                .filter(e -> e.getAccountType() == AccountTypes.CHECKING)
                .filter(e -> accountBalances.containsKey(e.getAccountId()))
                .map(e -> AccountEntity.toCreditCardAccount(e, accountBalances.get(e.getAccountId())))
                .collect(Collectors.toList());
    }

}
