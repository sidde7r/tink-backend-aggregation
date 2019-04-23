package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsV31Response extends BaseResponse<List<AccountEntity>>
        implements AccountStream {

    public static Optional<TransactionalAccount> toTransactionalAccount(
            AccountsV31Response accounts, AccountBalanceV31Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER
                                        .isTransactionalAccount(e.getRawAccountSubType()))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(
            AccountsV31Response accounts, AccountBalanceV31Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(
                                        e.getRawAccountSubType()))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));
    }

    public Stream<AccountEntity> stream() {
        return getData().stream();
    }
}
