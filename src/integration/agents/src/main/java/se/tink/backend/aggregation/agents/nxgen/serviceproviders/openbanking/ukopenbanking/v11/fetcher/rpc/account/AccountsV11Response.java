package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsV11Response extends BaseResponse<List<AccountEntity>>
        implements AccountStream {

    @Override
    public Stream<? extends IdentifiableAccount> stream() {
        return getData().stream();
    }

    public static Optional<TransactionalAccount> toTransactionalAccount(
            AccountsV11Response accounts, AccountBalanceV11Response balance) {

        return accounts.getData().stream()
                .filter(e -> e.getAccountType().equals(AccountTypes.CHECKING))
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(
            AccountsV11Response accounts, AccountBalanceV11Response balances) {

        // TODO: v11 does not have clear documentation on how one determines an account to be a
        // credit card.
        // TODO  Test data does not include credit cards, this will be revisited when we have data
        // that includes this.
        return Optional.empty();
    }
}
