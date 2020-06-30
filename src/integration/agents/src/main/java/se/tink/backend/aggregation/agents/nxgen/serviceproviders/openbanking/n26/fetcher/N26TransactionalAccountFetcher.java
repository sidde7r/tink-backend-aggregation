package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.refresh.CheckingAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.NextGenAccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity.ProviderAccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class N26TransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private static final String CHECKING_ACCOUNT_IDENTIFIER = "CHECKING";

    private final N26ApiClient apiClient;

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .filter(
                        account ->
                                CHECKING_ACCOUNT_IDENTIFIER.equals(
                                        account.getAccountDetails().getType()))
                .map(this::mapAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient.getAccountTransactions(account.getApiIdentifier(), key);
    }

    private Optional<TransactionalAccount> mapAccount(AccountEntity account) {
        final IbanIdentifier ibanId =
                mapIban(account.getAccountDetails().getProviderAccountDetails());
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(mapBalance(apiClient.getAccountBalance(account.getId())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(ibanId.getIban())
                                .withAccountNumber(ibanId.getIban())
                                .withAccountName(account.getName())
                                .addIdentifier(ibanId)
                                .build())
                .setApiIdentifier(account.getId())
                .build();
    }

    private BalanceModule mapBalance(AccountBalanceResponse accountBalanceResponse) {
        final AmountEntity availableBalance =
                Optional.ofNullable(accountBalanceResponse)
                        .map(AccountBalanceResponse::getBalance)
                        .map(AccountBalanceEntity::getAvailable)
                        .orElseThrow(
                                () ->
                                        new CheckingAccountRefreshException(
                                                "Available balance not found in response"));

        final AmountEntity currentBalance =
                Optional.of(accountBalanceResponse)
                        .map(AccountBalanceResponse::getBalance)
                        .map(AccountBalanceEntity::getCurrent)
                        .orElseThrow(
                                () ->
                                        new CheckingAccountRefreshException(
                                                "Current balance not found in response"));

        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                currentBalance.getValue(), currentBalance.getCurrency()))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(
                                availableBalance.getValue(), availableBalance.getCurrency()))
                .build();
    }

    private IbanIdentifier mapIban(ProviderAccountDetailsEntity providerAccountDetails) {
        return Optional.ofNullable(providerAccountDetails)
                .map(ProviderAccountDetailsEntity::getNextGenPsd2AccountDetails)
                .map(NextGenAccountDetailsEntity::getIban)
                .map(IbanIdentifier::new)
                .orElseThrow(
                        () -> new CheckingAccountRefreshException("IBAN not found in response"));
    }
}
