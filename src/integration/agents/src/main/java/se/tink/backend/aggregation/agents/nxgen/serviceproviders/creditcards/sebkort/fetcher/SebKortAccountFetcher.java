package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.CardContractEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SebKortAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SebKortAccountFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final CardsResponse cardsResponse = apiClient.fetchCards();

        final List<CardContractEntity> cardContracts = cardsResponse.getCardContracts();
        final Map<String, CardAccountEntity> accountsHashMap = cardsResponse.getCardAccountsHashMap();

        return cardContracts
                .stream()
                .flatMap(contract -> contract.toTinkCreditCardAccounts(accountsHashMap).stream())
                .collect(Collectors.toList());
    }
}
