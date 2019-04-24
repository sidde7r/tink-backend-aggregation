package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditAccountsResponse;
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
                .map(this::enrichAccountWithBalance)
                .collect(Collectors.toList());
    }

    private CreditCardAccount enrichAccountWithBalance(AccountEntity account) {
        return account.toCreditCardAccount(client.getAccountBalance(account.getAccountId()));
    }
}
