package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccountConsent;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.CardAccountsItem;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FinecoBankCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final FinecoBankApiClient finecoBankApiClient;
    private final PersistentStorage persistentStorage;

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

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        if (isEmptyCreditAccountConsent(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        try {
            return PaginatorResponseImpl.create(
                    finecoBankApiClient
                            .getCreditTransactions(account, fromDate, toDate)
                            .getTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == ErrorMessages.ACCESS_EXCEEDED_ERROR_CODE
                    || e.getResponse().getStatus() == ErrorMessages.BAD_REQUEST_ERROR_CODE) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
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
}
