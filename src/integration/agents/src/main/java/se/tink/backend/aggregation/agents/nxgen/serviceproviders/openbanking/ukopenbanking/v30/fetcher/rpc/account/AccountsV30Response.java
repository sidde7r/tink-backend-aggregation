package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.AccountStream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.UkOpenBankingV30Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsV30Response extends BaseResponse<List<AccountEntity>>
        implements AccountStream {

    @JsonIgnore
    private static BiPredicate<AccountEntity, List<AccountTypes>> isOneOfType =
            (account, types) -> {
                AccountTypes accountType =
                        UkOpenBankingV30Constants.ACCOUNT_TYPE_MAPPER
                                .translate(account.getRawAccountSubType())
                                .orElse(AccountTypes.OTHER);
                return types.contains(accountType);
            };

    private static BiPredicate<AccountEntity, AccountTypes> isOfType =
            (account, type) -> isOneOfType.test(account, Arrays.asList(type));

    public static Optional<TransactionalAccount> toTransactionalAccount(
            AccountsV30Response accounts, AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                isOneOfType.test(
                                        e,
                                        Arrays.asList(AccountTypes.CHECKING, AccountTypes.SAVINGS)))
                .findFirst()
                .map(e -> AccountEntity.toTransactionalAccount(e, balance.getBalance()));
    }

    public static Optional<CreditCardAccount> toCreditCardAccount(
            AccountsV30Response accounts, AccountBalanceV30Response balance) {

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(e -> isOfType.test(e, AccountTypes.CREDIT_CARD))
                .findFirst()
                .map(e -> AccountEntity.toCreditCardAccount(e, balance.getBalance()));
    }

    public Stream<AccountEntity> stream() {
        return getData().stream();
    }
}
