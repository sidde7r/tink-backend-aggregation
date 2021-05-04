package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.CreditDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ICSAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final ICSApiClient client;

    public ICSAccountFetcher(ICSApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Optional.ofNullable(client.getAllAccounts())
                .map(CreditAccountsResponse::getData)
                .map(AccountDataEntity::getAccount)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(account -> account.getCreditCardEntity().isActive())
                .map(this::enrichAccountWithBalanceAndHolderName)
                .collect(Collectors.toList());
    }

    private CreditCardAccount enrichAccountWithBalanceAndHolderName(AccountEntity account) {
        String accountId = account.getAccountId();
        return account.toCreditCardAccount(
                client.getAccountBalance(accountId), getHolderName(accountId));
    }

    private String getHolderName(String accountId) {

        CreditTransactionsResponse creditTransactionsResponse =
                client.getTransactionsByDate(
                        accountId, LocalDate.now().minusDays(30), LocalDate.now());

        return Optional.ofNullable(creditTransactionsResponse)
                .map(CreditTransactionsResponse::getData)
                .map(CreditDataEntity::getTransactions)
                .map(Collection::stream)
                .map(Stream::findFirst)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TransactionEntity::getCreditCardHolderName)
                .orElse(null);
    }
}
