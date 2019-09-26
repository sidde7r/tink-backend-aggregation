package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SkandiaBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final AggregationLogger LOG =
            new AggregationLogger(SkandiaBankenCreditCardFetcher.class);
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenCreditCardFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            final String creditCardsResponse = apiClient.fetchCreditCards().toString();
            LOG.infoExtraLong(creditCardsResponse, LogTags.CREDIT_CARDS);
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
