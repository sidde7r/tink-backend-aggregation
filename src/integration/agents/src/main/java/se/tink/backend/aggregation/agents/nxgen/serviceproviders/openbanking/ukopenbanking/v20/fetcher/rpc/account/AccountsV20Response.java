package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.rpc.account;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.IdentifiableAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsV20Response extends BaseResponse<List<AccountEntity>>
        implements AccountStream {

    public static Optional<TransactionalAccount> toTransactionalAccount(
            AccountsV20Response accounts, AccountBalanceV20Response balance) {

        return accounts.getData().stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                UkOpenBankingV20Constants.ACCOUNT_TYPE_MAPPER.isOneOf(
                                        e.getRawAccountSubType(),
                                        Arrays.asList(AccountTypes.CHECKING, AccountTypes.SAVINGS)))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(
            AccountsV20Response accounts, AccountBalanceV20Response balance) {

        return accounts.getData().stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                UkOpenBankingV20Constants.ACCOUNT_TYPE_MAPPER.isOf(
                                        e.getRawAccountSubType(), AccountTypes.CREDIT_CARD))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));
    }

    @Override
    public Stream<? extends IdentifiableAccount> stream() {
        return getData().stream();
    }
}
