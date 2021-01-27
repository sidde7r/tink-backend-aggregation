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
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FinecoBankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final FinecoBankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public FinecoBankTransactionalAccountFetcher(
            FinecoBankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        if (apiClient.isEmptyTransactionalAccountBalanceConsent()) {
            return Collections.emptyList();
        }

        return this.apiClient.fetchAccounts().getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (isEmptyTransactionAccountConsent(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        try {
            return PaginatorResponseImpl.create(
                    apiClient.getTransactions(account, fromDate, toDate).getTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == ErrorMessages.ACCESS_EXCEEDED_ERROR_CODE
                    || (e.getResponse().getStatus() == ErrorMessages.BAD_REQUEST_ERROR_CODE
                            && e.getResponse()
                                    .getBody(String.class)
                                    .contains(ErrorMessages.PERIOD_INVALID_ERROR))) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }

    private boolean isEmptyTransactionAccountConsent(TransactionalAccount transactionalAccount) {
        List<AccountConsent> transactionsConsents =
                persistentStorage
                        .get(
                                StorageKeys.TRANSACTIONS_CONSENTS,
                                new TypeReference<List<AccountConsent>>() {})
                        .orElse(Collections.emptyList());
        for (AccountConsent transactionsConsent : transactionsConsents) {
            String iban = transactionsConsent.getIban();
            if (!Strings.isNullOrEmpty(iban)
                    && iban.equals(transactionalAccount.getIdModule().getAccountNumber())) {
                return false;
            }
        }

        return true;
    }
}
