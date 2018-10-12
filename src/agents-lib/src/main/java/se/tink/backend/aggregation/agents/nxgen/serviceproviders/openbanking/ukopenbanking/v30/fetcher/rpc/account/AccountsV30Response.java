package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountsV30Response extends BaseResponse<List<AccountEntity>> implements AccountStream {

    public Stream<AccountEntity> stream() {
        return getData().stream();
    }

    public static Optional<TransactionalAccount> toTransactionalAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CHECKING))
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CREDIT_CARD))
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));
    }
}
