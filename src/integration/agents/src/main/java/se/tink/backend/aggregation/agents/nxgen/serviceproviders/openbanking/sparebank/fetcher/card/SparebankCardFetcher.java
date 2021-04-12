package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@AllArgsConstructor
@Slf4j
public class SparebankCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SparebankApiClient apiClient;
    private final SparebankCardMapper cardMapper;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            return apiClient.fetchCards().getCardAccounts().stream()
                    .map(this::enrichWithBalanceAndTransform)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (bankDoesNotSupportCreditCards(e)) {
                log.warn("[Sparebank][CC] Bank does not support CC endpoint");
                return Collections.emptyList();
            }
            throw e;
        }
    }

    private Optional<CreditCardAccount> enrichWithBalanceAndTransform(CardEntity cardEntity) {
        return cardMapper.toTinkCardAccount(
                cardEntity, apiClient.fetchCardBalances(cardEntity.getResourceId()).getBalances());
    }

    private boolean bankDoesNotSupportCreditCards(HttpResponseException e) {
        return HttpStatus.SC_NOT_FOUND == e.getResponse().getStatus();
    }
}
