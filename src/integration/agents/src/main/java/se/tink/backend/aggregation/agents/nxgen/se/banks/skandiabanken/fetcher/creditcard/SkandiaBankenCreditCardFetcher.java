package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SkandiaBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenCreditCardFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            FetchCreditCardsResponse fetchCreditCardsResponse = apiClient.fetchCreditCards();

            ListUtils.emptyIfNull(fetchCreditCardsResponse.getCards()).stream()
                    .filter(cardEntity -> !cardEntity.isDebitCard())
                    .findAny()
                    .ifPresent(
                            cardEntity ->
                                    log.info(
                                            "Possible credit card found, typeName {}",
                                            cardEntity.getTypeName()));

        } catch (HttpResponseException e) {
            ErrorResponse error = e.getResponse().getBody(ErrorResponse.class);
            // Check if expected error response for no available creditcards or keep throwing.
            if (!error.isNoCreditCardsError()) {
                throw e;
            }
        }
        return Collections.emptyList();
    }
}
