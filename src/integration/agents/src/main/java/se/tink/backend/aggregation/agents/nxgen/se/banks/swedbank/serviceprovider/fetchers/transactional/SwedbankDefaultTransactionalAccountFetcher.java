package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class SwedbankDefaultTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, LinkEntity>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final SwedbankDefaultApiClient apiClient;
    private final PersistentStorage persistentStorage;

    // FIX temporary for Swedbanks problem with pagination
    // store transactions ids we have already seen by account
    private Map<String, Set<String>> transactionIdsSeen = new HashMap<>();
    // store pseudo keys we have already seen by account
    private Map<String, Set<String>> pseudoKeysSeen = new HashMap<>();
    //

    public SwedbankDefaultTransactionalAccountFetcher(
            SwedbankDefaultApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            EngagementOverviewResponse engagementOverviewResponse =
                    bankProfile.getEngagementOverViewResponse();

            accounts.addAll(getTransactionAccounts(bankProfile, engagementOverviewResponse));
            accounts.addAll(
                    getTransactionDisposalAccounts(bankProfile, engagementOverviewResponse));
            accounts.addAll(getSavingAccounts(bankProfile, engagementOverviewResponse));
        }

        if (apiClient.getBankProfiles().size() > 1) {
            debugLogAccounts(accounts);
        }

        return accounts;
    }

    private List<TransactionalAccount> getSavingAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getSavingAccounts().stream()
                .filter(account -> !account.isInvestmentAccount())
                .map(
                        account ->
                                account.toTransactionalAccount(
                                        bankProfile, getEngagementTransactionsResponse(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<TransactionalAccount> getTransactionDisposalAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getTransactionDisposalAccounts().stream()
                .filter(account -> !account.isInvestmentAccount())
                .map(
                        account ->
                                account.toTransactionalAccount(
                                        bankProfile, getEngagementTransactionsResponse(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<TransactionalAccount> getTransactionAccounts(
            BankProfile bankProfile, EngagementOverviewResponse engagementOverviewResponse) {
        return engagementOverviewResponse.getTransactionAccounts().stream()
                .filter(account -> !account.isInvestmentAccount())
                .map(
                        account ->
                                account.toTransactionalAccount(
                                        bankProfile, getEngagementTransactionsResponse(account)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private EngagementTransactionsResponse getEngagementTransactionsResponse(
            AccountEntity account) {
        LinkEntity linkEntity = account.getLinkOrNull();
        if (linkEntity != null) {
            return apiClient.engagementTransactions(linkEntity);
        }
        return null;
    }

    // DEBUG to see why refresh transactions fails
    protected void debugLogAccounts(List<TransactionalAccount> accounts) {
        try {
            for (TransactionalAccount account : accounts) {
                String accountNumber = account.getAccountNumber();

                BankProfile bankProfile =
                        account.getFromTemporaryStorage(
                                        SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                                .orElse(null);

                String bankProfileId =
                        isBankProfileBankPresent(bankProfile)
                                ? bankProfile.getBank().getBankId()
                                : "N/A";

                log.info(
                        String.format(
                                "Swedbank_multiprofile Account [%s], BankProfileId [%s]",
                                accountNumber, bankProfileId));
            }
        } catch (Exception e) {
            log.warn("Swedbank_multiprofile Failed to log info for multiprofile user", e);
        }
    }

    private boolean isBankProfileBankPresent(BankProfile bankProfile) {
        return bankProfile != null && bankProfile.getBank() != null;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {

        BankProfile bankProfile = getBankProfileFromStorage(account);

        apiClient.selectProfile(bankProfile);

        Optional<PaymentsConfirmedResponse> paymentsConfirmedResponse =
                apiClient.paymentsConfirmed();

        if (paymentsConfirmedResponse.isPresent()) {
            return paymentsConfirmedResponse
                    .get()
                    .toTinkUpcomingTransactions(account.getAccountNumber());
        }

        log.info("Fetching of upcoming payments is not possible for this user.");
        return Collections.emptyList();
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {

        BankProfile bankProfile = getBankProfileFromStorage(account);

        apiClient.selectProfile(bankProfile);

        if (key != null) {
            return fetchTransactions(account, key);
        }

        LinkEntity nextLink =
                account.getFromTemporaryStorage(
                                SwedbankBaseConstants.StorageKey.NEXT_LINK, LinkEntity.class)
                        .orElse(null);

        TransactionKeyPaginatorResponseImpl<LinkEntity> transactionKeyPaginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        if (nextLink == null) {
            // Return empty response
            return transactionKeyPaginatorResponse;
        }

        // Every time we fetch the transactions for an account we get all reserved transactions.
        // This is a hack to only get the reserved transactions from the first response.
        EngagementTransactionsResponse engagementTransactionsResponse =
                fetchTransactions(account, nextLink);

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(engagementTransactionsResponse.toTransactions());
        transactions.addAll(engagementTransactionsResponse.reservedTransactionsToTransactions());

        transactionKeyPaginatorResponse.setNext(engagementTransactionsResponse.nextKey());
        transactionKeyPaginatorResponse.setTransactions(transactions);

        return transactionKeyPaginatorResponse;
    }

    private BankProfile getBankProfileFromStorage(TransactionalAccount account) {
        return account.getFromTemporaryStorage(
                        SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessage.NO_BANK_ID));
    }

    protected EngagementTransactionsResponse fetchTransactions(
            TransactionalAccount account, LinkEntity key) {
        try {
            EngagementTransactionsResponse rawResponse = apiClient.engagementTransactions(key);

            // temporary fix to detect and filter duplicate transactions
            return filterTransactionDuplicates(account, rawResponse);
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();
            // check if we are paginating and receive INTERNAL SERVER ERROR
            // In that case we have a temporary fix to return "done". This is because Swedbank
            // have an issue with their paginated transaction fetching currently. Remove
            // this temporary fix when we now Swedbank provides a working paginating endpoint again
            // NB! We mark the credentials receiving this error for future clean up activities.
            // PersistentStorage is used for setting mark
            if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    || response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                // mark credential with PAGINATION_ERROR in persistent storage
                persistentStorage.put(
                        SwedbankBaseConstants.PaginationError.PAGINATION_ERROR,
                        account.getAccountNumber());
                // Log to notify we still have the problem
                log.warn(SwedbankBaseConstants.PaginationError.PAGINATION_ERROR_MSG, hre);

                // return fetching is "done"
                return new EngagementTransactionsResponse();
            }
            // http error 404 that occurs rarely during pagination, but it will work if the user
            // tries again.
            if (response.getStatus() == HttpStatus.SC_NOT_FOUND) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }

            throw hre;
        }
    }

    //
    // FIX for Swedbank pagination problems
    //
    // Remove when Swedbank has fixed the problem
    //
    private EngagementTransactionsResponse filterTransactionDuplicates(
            TransactionalAccount account, EngagementTransactionsResponse rawResponse) {

        // no need to filter non-existing data
        if (rawResponse.getTransactions() == null) {
            return rawResponse;
        }

        // fetch all transaction ids we have seen for this account
        Set<String> fetchedTransactionIds = getFetchedTransactionsIds(account.getAccountNumber());
        Set<String> newPseudoKeys = new HashSet<>();
        // fetch all pseudo keys we have seen for this account
        Set<String> fetchedPseudoKeys = getFetchedPseudoKeys(account.getAccountNumber());

        List<TransactionEntity> filteredTransactions =
                rawResponse.getTransactions().stream()
                        .filter(
                                transaction ->
                                        checkIfNewAndStoreTransactionId(
                                                transaction, fetchedTransactionIds))
                        .filter(
                                transaction ->
                                        checkIfNewAndStorePseudoKey(
                                                transaction, fetchedPseudoKeys, newPseudoKeys))
                        .collect(Collectors.toList());

        // add all pseudo keys we have for this batch of transactions
        fetchedPseudoKeys.addAll(newPseudoKeys);

        rawResponse.getTransactions().clear();
        rawResponse.getTransactions().addAll(filteredTransactions);

        return rawResponse;
    }

    // fetch cached transaction ids
    private Set<String> getFetchedTransactionsIds(String accountNumber) {
        if (!transactionIdsSeen.containsKey(accountNumber)) {
            transactionIdsSeen.clear();
            transactionIdsSeen.put(accountNumber, new HashSet<>());
        }

        return transactionIdsSeen.get(accountNumber);
    }

    // fetch cached pseudo keys
    private Set<String> getFetchedPseudoKeys(String accountNumber) {
        if (!pseudoKeysSeen.containsKey(accountNumber)) {
            pseudoKeysSeen.clear();
            pseudoKeysSeen.put(accountNumber, new HashSet<>());
        }

        return pseudoKeysSeen.get(accountNumber);
    }

    // filter transactions to only allow new transaction ids
    // add transaction ids to seenTransaction ids
    private boolean checkIfNewAndStoreTransactionId(
            TransactionEntity transaction, Set<String> seenTransactionIds) {
        String txId = transaction.getId();

        // if transaction id is not set, we cannot filter, accept transaction
        if (Strings.isNullOrEmpty(txId)) {
            return true;
        }

        boolean isNewId = !seenTransactionIds.contains(txId);
        seenTransactionIds.add(txId);

        return isNewId;
    }

    // filter transactions to only allow new pseudo keys
    // add new pseudo keys to tmp set during current batch and add them to seenPseudoKeys
    // after entire batch is done, this is to avoid removing non-duplicate transactions.
    // It is not unusual with identical transactions
    private boolean checkIfNewAndStorePseudoKey(
            TransactionEntity transaction, Set<String> seenKeys, Set<String> newKeys) {

        // if transaction id is set we have decided it is not a duplicate
        if (!Strings.isNullOrEmpty(transaction.getId())) {
            return true;
        }

        String key = transaction.getPseudoKey();
        newKeys.add(key);

        return !seenKeys.contains(key);
    }
}
