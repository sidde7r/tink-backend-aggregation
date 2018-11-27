package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class SebKortAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SebKortAccountFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        CardsResponse cards = apiClient.fetchCards();
        UserEntity user = cards.getUser();

        Map<String, CardAccountEntity> accounts =
                cards.getCardAccounts()
                        .stream()
                        .collect(Collectors.toMap(CardAccountEntity::getId, account -> account));

        List<CreditCardAccount> collect =
                cards.getCardContracts()
                        .stream()
                        .flatMap(
                                contract ->
                                        contract.getCards()
                                                .stream()
                                                .map(
                                                        card ->
                                                                card.toTinkAccount(
                                                                        user,
                                                                        contract,
                                                                        accounts.get(
                                                                                contract
                                                                                        .getCardAccountId()),
                                                                        card)))
                        .collect(Collectors.toList());

        return collect;
    }
}
