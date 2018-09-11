package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionFetcher<CreditCardAccount> {
    private final static AggregationLogger log = new AggregationLogger(CreditCardFetcher.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CreditCardFetcher(SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        String loginResponseString = sessionStorage.get(SantanderEsConstants.Storage.LOGIN_RESPONSE, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        SantanderEsConstants.LogMessages.LOGIN_RESPONSE_NOT_FOUND));

        try {
            LoginResponse loginResponse = SantanderEsXmlUtils.parseXmlStringToJson(
                    loginResponseString, LoginResponse.class);
            List<CardEntity> cardList = loginResponse.getCards();

            if (cardList == null || cardList.isEmpty()) {
                return Collections.emptyList();
            }

            logNonDebitCards(cardList, loginResponse.getUserData());
        } catch (Exception e) {
            log.warn("Something went wrong when logging credit card accounts");
        }

        return Collections.emptyList();
    }

    // Logging all non debit cards, don't know exactly what the credit cards will be called, probably
    // "crédito", but blacklisting just in case.
    private void logNonDebitCards(List<CardEntity> cardList, UserData userData) {
        String userDataXml = SantanderEsXmlUtils.parseJsonToXmlString(userData);
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(3).withDayOfMonth(1).minusDays(1);
        boolean pagination = false;

        cardList.stream()
                .filter(cardEntity -> !SantanderEsConstants.AccountTypes.DEBIT_CARD_TYPE.equalsIgnoreCase(
                        cardEntity.getCardType()))
                .forEach(cardEntity -> {
                    log.infoExtraLong(SerializationUtils.serializeToString(cardEntity),
                            SantanderEsConstants.Tags.CREDIT_CARD_ACCOUNT);

                    try {
                        // log credit card details, just so we know what we are missing
                        String creditCardDetailsResponse = apiClient.fetchCreditCardDetails(userDataXml, cardEntity.getCardNumber());
                        log.infoExtraLong(creditCardDetailsResponse,
                                SantanderEsConstants.Tags.CREDIT_CARD_ACCOUNT);
                    } catch (Exception e) {
                        log.info(SantanderEsConstants.Tags.CREDIT_CARD_ACCOUNT.toString() + " Could not fetch details for credit card", e);
                    }

                    logNonDebitCardsTransactions(userDataXml, cardEntity, fromDate, toDate, pagination);
                });
    }

    // Logging transactions for non debit cards.
    private void logNonDebitCardsTransactions(String userDataXml, CardEntity card, LocalDate fromDate, LocalDate toDate, boolean pagination) {
        try {
            String creditCardDetailsResponse = apiClient.fetchCreditCardTransactions(userDataXml, card, fromDate, toDate, pagination);
            log.infoExtraLong(creditCardDetailsResponse, SantanderEsConstants.Tags.CREDIT_CARD_TRANSACTION);
        } catch (Exception e) {
            log.info(SantanderEsConstants.Tags.CREDIT_CARD_TRANSACTION.toString() + " Could not fetch transactions for credit card", e);
        }
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
