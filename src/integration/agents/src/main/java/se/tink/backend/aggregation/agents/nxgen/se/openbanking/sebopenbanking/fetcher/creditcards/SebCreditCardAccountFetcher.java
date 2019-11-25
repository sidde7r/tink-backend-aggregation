package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.creditcards;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebStorage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.BookedEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.PendingEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.entities.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SebCreditCardAccountFetcher<A extends Account>
        implements AccountFetcher<CreditCardAccount> {

    private final SebApiClient apiClient;
    private final SebStorage instanceStorage;
    private final Map<String, CreditCardAccount> accountNumberAccountMap;

    public SebCreditCardAccountFetcher(SebApiClient client, SebStorage instanceStorage) {
        this.apiClient = client;
        this.instanceStorage = instanceStorage;
        accountNumberAccountMap = new HashMap<>();
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Collections.EMPTY_LIST;

        //  apiClient.fetchCardAccounts().toTinkAccounts().stream()
        //          .forEach(
        //                  creditCardAccount ->
        // fetchAndSaveCreditCardTransactions(creditCardAccount));
        // return accountNumberAccountMap.values();
    }

    /**
     * This method fetches all the {@link TransactionsEntity} for a {@link CreditCardAccount} and
     * checks each of them for creditCardNumber. If it finds a new creditCardNumber also known as
     * sub-card(other then that is already present in the CreditCardAccount) that is mapped to any
     * of the {@link PendingEntity} Or {@link BookedEntity}, it creates a new {@link
     * CreditCardAccount} and stores it in the accountNumberAccountMap.
     *
     * <p>All the {@link TransactionsEntity}(s) are stored in the instanceStorage and then used in
     * {@link SebCreditCardTransactionsFetcher} to get CreditCardTransaction for each account
     *
     * @param account - {@link CreditCardAccount} for which we need to fetch {@link
     *     FetchCardAccountsTransactions} and check if there is more then one creditCardNumber
     *     present in the transaction.
     */
    private void fetchAndSaveCreditCardTransactions(CreditCardAccount account) {
        // put main CreditCard to accountNumber-account Map
        accountNumberAccountMap.putIfAbsent(account.getAccountNumber(), account);

        LocalDate now = LocalDate.now();
        LocalDate fromDate = LocalDate.of(now.getYear(), now.getMonth(), 1);
        LocalDate toDate = now;

        boolean canFetchMore = true;
        while (canFetchMore) {
            try {
                FetchCardAccountsTransactions response =
                        apiClient.fetchCardTransactions(
                                account.getApiIdentifier(), fromDate, toDate);
                checkSubCreditCards(account, response.getTransactions());
                instanceStorage.saveCreditCardTransactionResponse(response.getTransactions());
                fromDate = fromDate.minusMonths(1);
                toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                        && e.getResponse().getBody(ErrorResponse.class).isEndOfPagingError()) {
                    canFetchMore = false;
                } else {
                    throw e;
                }
            }
        }
    }

    private void checkSubCreditCards(CreditCardAccount account, TransactionsEntity transactions) {
        checkAndCreateSubCreditCardInBookedTransaction(account, transactions.getBooked());
        checkAndCreateSubCreditCardInPendingTransaction(account, transactions.getPending());
    }

    private void checkAndCreateSubCreditCardInBookedTransaction(
            CreditCardAccount account, List<BookedEntity> bookedTransactions) {
        bookedTransactions.stream()
                .filter(bookedEntity -> !Strings.isNullOrEmpty(bookedEntity.getMaskedPan()))
                .forEach(
                        bookedEntity ->
                                checkAndCreateNewCreditCardAccount(
                                        account,
                                        bookedEntity.getMaskedPan(),
                                        bookedEntity.getNameOnCard()));
    }

    private void checkAndCreateSubCreditCardInPendingTransaction(
            CreditCardAccount account, List<PendingEntity> pendingEntities) {
        pendingEntities.stream()
                .filter(pendingEntity -> !Strings.isNullOrEmpty(pendingEntity.getMaskedPan()))
                .forEach(
                        pendingEntity ->
                                checkAndCreateNewCreditCardAccount(
                                        account,
                                        pendingEntity.getMaskedPan(),
                                        pendingEntity.getNameOnCard()));
    }

    private void checkAndCreateNewCreditCardAccount(
            CreditCardAccount account, String creditCardNumber, String nameOnCard) {
        if (!accountNumberAccountMap.containsKey(creditCardNumber)) {
            CreditCardAccount subCardAccount =
                    SebUtils.createSubCreditCard(account, creditCardNumber, nameOnCard);
            accountNumberAccountMap.put(subCardAccount.getAccountNumber(), subCardAccount);
        }
    }
}
