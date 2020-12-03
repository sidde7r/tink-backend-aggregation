package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AllArgsConstructor
public class SparebankCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SparebankApiClient apiClient;
    private final SparebankCardMapper cardMapper;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCards().getCardAccounts().stream()
                .map(this::enrichWithBalanceAndTransform)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<CreditCardAccount> enrichWithBalanceAndTransform(CardEntity cardEntity) {
        return cardMapper.toTinkCardAccount(
                cardEntity, apiClient.fetchCardBalances(cardEntity.getResourceId()).getBalances());
    }
}
