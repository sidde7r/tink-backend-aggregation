package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class FinecoBankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final FinecoBankApiClient apiClient;
    private final FinecoStorage storage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        if (isEmptyTransactionalAccountBalanceConsent()) {
            return Collections.emptyList();
        }

        return apiClient.fetchAccounts(storage.getConsentId()).getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isEmptyTransactionalAccountBalanceConsent() {
        return storage.getBalancesConsents().stream()
                .allMatch(balancesItem -> Strings.isNullOrEmpty(balancesItem.getIban()));
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        if (!canFetchTransactionsForAccount(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        try {
            return PaginatorResponseImpl.create(
                    apiClient
                            .getTransactions(storage.getConsentId(), account, fromDate, toDate)
                            .getTinkTransactions());
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

    private boolean canFetchTransactionsForAccount(TransactionalAccount transactionalAccount) {
        String accountNumber = transactionalAccount.getIdModule().getAccountNumber();
        return storage.getTransactionsConsents().stream()
                .anyMatch(x -> accountNumber.equals(x.getIban()));
    }
}
