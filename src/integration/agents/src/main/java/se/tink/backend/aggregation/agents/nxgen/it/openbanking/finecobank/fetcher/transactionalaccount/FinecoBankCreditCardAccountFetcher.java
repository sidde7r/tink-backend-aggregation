package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.CardAccountsItem;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class FinecoBankCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionMonthPaginator<CreditCardAccount> {

    private static final int MAX_MONTHS_ALLOWED_TO_FETCH = 3;
    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;
    private int monthsRequestCounter = 0;

    public FinecoBankCreditCardAccountFetcher(
            FinecoBankApiClient finecoBankApiClient, PersistentStorage persistentStorage) {
        this.finecoBankApiClient = finecoBankApiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        if (finecoBankApiClient.isEmptyCreditCardAccountBalanceConsent()) {
            return Collections.emptyList();
        }

        return this.finecoBankApiClient.fetchCreditCardAccounts().getCardAccounts().stream()
                .map(CardAccountsItem::toTinkCreditAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
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
        if (isEmptyCreditAccountConsent(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }
        LocalDate fromDateAdjusted = prepareFromDate(year, month);
        try {
            if (monthsRequestCounter == MAX_MONTHS_ALLOWED_TO_FETCH) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            return finecoBankApiClient.getCreditTransactions(account, fromDateAdjusted);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == ErrorMessages.ACCESS_EXCEEDED_ERROR_CODE
                    || e.getResponse().getStatus() == ErrorMessages.BAD_REQUEST_ERROR_CODE) {
                log.warn("Fineco returned 429 - verify number of requests", e);
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        } finally {
            monthsRequestCounter++;
        }
    }

    private boolean isEmptyCreditAccountConsent(CreditCardAccount creditCardAccount) {
        List<AccountConsent> transactionsConsents =
                persistentStorage
                        .get(
                                StorageKeys.TRANSACTION_ACCOUNTS,
                                new TypeReference<List<AccountConsent>>() {})
                        .orElse(Collections.emptyList());
        for (AccountConsent transactionsConsent : transactionsConsents) {
            String maskedPan = transactionsConsent.getMaskedPan();
            if (!Strings.isNullOrEmpty(maskedPan)
                    && maskedPan.equals(creditCardAccount.getIdModule().getAccountNumber())) {
                return false;
            }
        }

        return true;
    }

    private LocalDate prepareFromDate(Year year, Month month) {
        return LocalDate.of(year.getValue(), month, 1);
    }
}
