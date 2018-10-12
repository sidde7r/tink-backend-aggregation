package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class AccountsV20Response extends BaseResponse<List<AccountEntity>> implements AccountStream {

    @Override
    public Stream<? extends IdentifiableAccount> stream() {
        return getData().stream();
    }

    public static Optional<TransactionalAccount> toTransactionalAccount(AccountsV20Response accounts,
            AccountBalanceV20Response balance) {

        return accounts.getData().stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CHECKING))
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(AccountsV20Response accounts,
            AccountBalanceV20Response balance) {

        return accounts.getData().stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CREDIT_CARD))
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));

    }
}
