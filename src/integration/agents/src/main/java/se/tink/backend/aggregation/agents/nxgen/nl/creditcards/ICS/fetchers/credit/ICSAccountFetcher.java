package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountEntity;
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
        try {
            CreditTransactionsResponse creditTransactionsResponse =
                    client.getTransactionsByDate(
                            accountId,
                            Date.valueOf(LocalDate.now()),
                            Date.valueOf(LocalDate.now().minusDays(30)));
            if (creditTransactionsResponse.getData().getTransactions().stream()
                    .findFirst()
                    .isPresent()) {
                return creditTransactionsResponse.getData().getTransactions().stream()
                        .findFirst()
                        .get()
                        .getCreditCardHolderName();
            } else {
                return "N/A";
            }
        } catch (RuntimeException e) {
            return "N/A";
        }
    }
}
