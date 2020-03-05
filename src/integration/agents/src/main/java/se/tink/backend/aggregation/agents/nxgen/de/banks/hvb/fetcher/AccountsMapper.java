package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule.of;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response.Account;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountsMapper {

    static final String BRANCH_NUMBER = "BRANCH_NUMBER";

    private static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "2")
                    .put(AccountTypes.SAVINGS, "6")
                    .build();

    List<TransactionalAccount> toTransactionalAccounts(AccountsResponse accountsResponse) {
        return Optional.of(accountsResponse)
                .map(AccountsResponse::getResponse)
                .map(Response::getAccounts)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(this::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTransactionalAccount(Account account) {
        String iban = account.getIban();
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(ACCOUNT_TYPE_MAPPER, account.getType())
                .withBalance(
                        of(ExactCurrencyAmount.of(account.getBalance(), account.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(account.getName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(account.getId())
                .setBankIdentifier(account.getId())
                .putInTemporaryStorage(BRANCH_NUMBER, account.getBranch())
                .build();
    }
}
