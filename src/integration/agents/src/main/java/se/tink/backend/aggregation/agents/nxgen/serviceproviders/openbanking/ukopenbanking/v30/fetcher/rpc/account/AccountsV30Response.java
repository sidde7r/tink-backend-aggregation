package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountsV30Response extends BaseResponse<List<AccountEntity>> implements AccountStream {

    public Stream<AccountEntity> stream() {
        return getData().stream();
    }

    public static Optional<TransactionalAccount> toTransactionalAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(e -> UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(e.getRawAccountSubType()))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(AccountsV30Response accounts,
            AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(e -> UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(e.getRawAccountSubType()))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));
    }
}
