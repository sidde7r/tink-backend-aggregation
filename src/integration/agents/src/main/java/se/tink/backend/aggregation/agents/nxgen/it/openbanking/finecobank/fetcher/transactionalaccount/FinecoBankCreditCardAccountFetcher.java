package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.BalancesItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.TransactionsItem;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity.CardAccountsItem;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
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

        if (isAvailableBalance()) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_BALANCES);
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

        if (isAvailableTransactions(account)) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONSENT_TRANSACTIONS);
        }

        try {
            return PaginatorResponseImpl.create(
                    finecoBankApiClient
                            .getCreditTransactions(account, fromDate, toDate)
                            .getTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == ErrorMessages.ACCESS_EXCEEDED_ERROR_CODE
                    || e.getResponse().getStatus() == ErrorMessages.PERIOD_INVALID_ERROR) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }

    private boolean isAvailableBalance() {
        List<BalancesItem> balancesItems =
                persistentStorage
                        .get(
                                StorageKeys.BALANCE_ACCOUNTS,
                                new TypeReference<List<BalancesItem>>() {})
                        .get();
        if (!balancesItems.isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean isAvailableTransactions(CreditCardAccount creditCardAccount) {
        List<TransactionsItem> transactionsItems =
                persistentStorage
                        .get(
                                StorageKeys.TRANSACTION_ACCOUNTS,
                                new TypeReference<List<TransactionsItem>>() {})
                        .get();
        if (!transactionsItems.isEmpty()) {
            for (TransactionsItem transactionsItem : transactionsItems) {
                if (!Strings.isNullOrEmpty(transactionsItem.getMaskedPan()))
                    if (transactionsItem
                            .getMaskedPan()
                            .equals(creditCardAccount.getIdModule().getAccountNumber())) {
                        return false;
                    }
            }
        }
        return true;
    }
}
