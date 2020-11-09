package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbCardMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AllArgsConstructor
public class DnbCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbCardMapper cardMapper;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCardAccounts(storage.getConsentId()).getCardAccounts().stream()
                .map(cardMapper::toTinkCardAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
