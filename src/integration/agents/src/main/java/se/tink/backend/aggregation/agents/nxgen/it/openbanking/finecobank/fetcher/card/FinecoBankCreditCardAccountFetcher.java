package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity.CardAccountsItem;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class FinecoBankCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionMonthPaginator<CreditCardAccount> {

    private static final int MAX_MONTHS_ALLOWED_TO_FETCH = 3;
    private static final int MAX_MONTHS_BG_REFRESH = 1;

    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;
    private final boolean isManual;

    private final Map<String, Integer> transactionsRequestsCounterPerApiIdentifier;

    public FinecoBankCreditCardAccountFetcher(
            FinecoBankApiClient finecoBankApiClient,
            PersistentStorage persistentStorage,
            boolean isManual) {
        this.finecoBankApiClient = finecoBankApiClient;
        this.persistentStorage = persistentStorage;
        this.isManual = isManual;

        this.transactionsRequestsCounterPerApiIdentifier = new HashMap<>();
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        if (finecoBankApiClient.isEmptyCreditCardAccountBalanceConsent()) {
            return Collections.emptyList();
        }

        return this.finecoBankApiClient.fetchCreditCardAccounts().getCardAccounts().stream()
                .map(CardAccountsItem::toCreditCardAccount)
                .collect(Collectors.toList());
    }

    /**
     * The card account transaction list is based on the DateFrom parameter: Fineco checks which
     * month is indicated in the request and will include, in the response, all the transactions for
     * that month.
     *
     * <p>Also Fineco allows us to fetch data only for 3 last months.
     *
     * @param account CreditCardAccount
     * @param year Starting year
     * @param month Starting month
     * @return Transactions for requested month e.g. January
     */
    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Year year, Month month) {

        if (!transactionsConsentForCreditCardAccountExists(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        if (hasReachedTransactionsRequestsLimit(account.getApiIdentifier())) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            LocalDate fromDateAdjusted = prepareFromDate(year, month);
            return finecoBankApiClient.getCreditTransactions(account, fromDateAdjusted);

        } catch (HttpResponseException e) {

            int responseStatus = e.getResponse().getStatus();
            if (responseStatus == ErrorMessages.ACCESS_EXCEEDED_ERROR_CODE) {
                log.warn("Fineco returned 429 - verify number of requests", e);
                return PaginatorResponseImpl.createEmpty(false);
            }
            if (responseStatus == ErrorMessages.BAD_REQUEST_ERROR_CODE) {
                log.warn("Fineco returned 400", e);
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw e;

        } finally {
            incrementRequestsCounter(account.getApiIdentifier());
        }
    }

    private boolean transactionsConsentForCreditCardAccountExists(
            CreditCardAccount creditCardAccount) {
        return getTransactionsConsentsFromStorage().stream()
                .anyMatch(
                        transactionsConsent -> {
                            String maskedPan = transactionsConsent.getMaskedPan();
                            String creditCardAccountNumber = creditCardAccount.getAccountNumber();

                            return Objects.equals(maskedPan, creditCardAccountNumber);
                        });
    }

    private List<AccountConsent> getTransactionsConsentsFromStorage() {
        return persistentStorage
                .get(
                        StorageKeys.TRANSACTIONS_CONSENTS,
                        new TypeReference<List<AccountConsent>>() {})
                .orElse(Collections.emptyList());
    }

    private boolean hasReachedTransactionsRequestsLimit(String apiIdentifier) {
        if (isConsentMaximum20MinutesOld()) {
            return false;
        }

        int requestsCount =
                transactionsRequestsCounterPerApiIdentifier.getOrDefault(apiIdentifier, 0);
        if (isManual) {
            return requestsCount == MAX_MONTHS_ALLOWED_TO_FETCH;
        }
        return requestsCount == MAX_MONTHS_BG_REFRESH;
    }

    // Fineco supports fetching transactions further back than 90 days if consent was given within
    // 20 minutes
    private boolean isConsentMaximum20MinutesOld() {
        final LocalDateTime consentCreated =
                persistentStorage.get(StorageKeys.TIMESTAMP, LocalDateTime.class).orElse(null);
        if (consentCreated == null) {
            return false;
        }
        return ChronoUnit.MINUTES.between(consentCreated, LocalDateTime.now()) < 20;
    }

    private LocalDate prepareFromDate(Year year, Month month) {
        return LocalDate.of(year.getValue(), month, 1);
    }

    private void incrementRequestsCounter(String apiIdentifier) {
        transactionsRequestsCounterPerApiIdentifier.compute(
                apiIdentifier, (key, counter) -> counter == null ? 1 : counter + 1);
    }
}
