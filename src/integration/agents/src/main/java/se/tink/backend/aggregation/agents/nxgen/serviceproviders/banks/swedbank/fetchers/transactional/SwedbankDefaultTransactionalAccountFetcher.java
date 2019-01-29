package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentsConfirmedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc.PortfolioHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, LinkEntity>, UpcomingTransactionFetcher<TransactionalAccount> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultTransactionalAccountFetcher.class);

    private final SwedbankDefaultApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private List<String> investmentAccountNumbers;

    // FIX temporary for Swedbanks problem with pagination
    // store transactions ids we have already seen by account
    private Map<String, Set<String>> transactionIdsSeen = new HashMap<>();
    // store pseudo keys we have already seen by account
    private Map<String, Set<String>> pseudoKeysSeen = new HashMap<>();
    //

    public SwedbankDefaultTransactionalAccountFetcher(SwedbankDefaultApiClient apiClient,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();

            accounts.addAll(engagementOverviewResponse.getTransactionAccounts().stream()
                    .map(account -> account.toTransactionalAccount(bankProfile))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
            accounts.addAll(engagementOverviewResponse.getTransactionDisposalAccounts().stream()
                    .map(account -> account.toTransactionalAccount(bankProfile))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
            accounts.addAll(engagementOverviewResponse.getSavingAccounts().stream()
                    // have not found any other way to filter out investment accounts from savings accounts
                    .filter(account -> !getInvestmentAccountNumbers().contains(account.getFullyFormattedNumber()))
                    .map(account -> account.toTransactionalAccount(bankProfile))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
            investmentAccountNumbers = null;
        }

        if (apiClient.getBankProfiles().size() > 1) {
            debugLogAccounts(accounts);
        }

        return accounts;
    }

    // DEBUG to see why refresh transactions fails
    private void debugLogAccounts(List<TransactionalAccount> accounts) {
        try {
            for (TransactionalAccount account : accounts) {
                String accountNumber = account.getAccountNumber();

                BankProfile bankProfile =
                        account.getFromTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                                .orElse(null);

                String bankProfileId = "N/A";
                if (bankProfile != null && bankProfile.getBank() != null) {
                    bankProfileId = bankProfile.getBank().getBankId();
                }

                log.info(String.format("Swedbank_multiprofile Account [%s], BankProfileId [%s]",
                        accountNumber, bankProfileId));
            }
        } catch (Exception e) {
            log.warn("Swedbank_multiprofile Failed to log info for multiprofile user");
        }
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        BankProfile bankProfile =
                account.getFromTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                        .orElseThrow(() -> new IllegalStateException("No bank profile specified"));
        apiClient.selectProfile(bankProfile);

        PaymentsConfirmedResponse paymentsConfirmedResponse = apiClient.paymentsConfirmed();

        return paymentsConfirmedResponse.toTinkUpcomingTransactions(account.getAccountNumber());
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {
        BankProfile bankProfile =
                account.getFromTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                        .orElseThrow(() -> new IllegalStateException("No bank profile specified"));
        apiClient.selectProfile(bankProfile);

        if (key != null) {
            return fetchTransactions(account, key);
        }

        LinkEntity nextLink =
                account.getFromTemporaryStorage(SwedbankBaseConstants.StorageKey.NEXT_LINK, LinkEntity.class)
                        .orElse(null);

        TransactionKeyPaginatorResponseImpl<LinkEntity> transactionKeyPaginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        if (nextLink == null) {
            // Return empty response
            return transactionKeyPaginatorResponse;
        }

        // Every time we fetch the transactions for an account we get all reserved transactions.
        // This is a hack to only get the reserved transactions from the first response.
        EngagementTransactionsResponse engagementTransactionsResponse = fetchTransactions(account, nextLink);

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(engagementTransactionsResponse.toTransactions());
        transactions.addAll(engagementTransactionsResponse.reservedTransactionsToTransactions());

        transactionKeyPaginatorResponse.setNext(engagementTransactionsResponse.nextKey());
        transactionKeyPaginatorResponse.setTransactions(transactions);

        return transactionKeyPaginatorResponse;
    }

    private EngagementTransactionsResponse fetchTransactions(TransactionalAccount account,
            LinkEntity key) {
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
            if (key != null && response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                // mark credential with PAGINATION_ERROR in persistent storage
                persistentStorage.put(SwedbankBaseConstants.PaginationError.PAGINATION_ERROR,
                        account.getAccountNumber());
                // Log to notify we still have the problem
                log.warn(SwedbankBaseConstants.PaginationError.PAGINATION_ERROR_MSG);

                // return fetching is "done"
                return new EngagementTransactionsResponse();
            }

            throw hre;
        }
    }

    // fetch all account number from investment accounts BUT savings accounts, this is because we want savings accounts
    // to be fetched by transactional fetcher to get any transactions
    private List<String> getInvestmentAccountNumbers() {

        if (investmentAccountNumbers == null) {
            String portfolioHoldingsString = apiClient.portfolioHoldings();
            PortfolioHoldingsResponse portfolioHoldings = SerializationUtils
                    .deserializeFromString(portfolioHoldingsString, PortfolioHoldingsResponse.class);

            investmentAccountNumbers = portfolioHoldings.investmentAccountNumbers();
        }

        return investmentAccountNumbers;
    }


    //
    // FIX for Swedbank pagination problems
    //
    // Remove when Swedbank has fixed the problem
    //
    private EngagementTransactionsResponse filterTransactionDuplicates(TransactionalAccount account,
            EngagementTransactionsResponse rawResponse) {

        // no need to filter non-existing data
        if (rawResponse.getTransactions() == null) {
            return rawResponse;
        }

        // fetch all transaction ids we have seen for this account
        Set<String> fetchedTransactionIds = getfetchedTransactionsIds(account.getAccountNumber());
        Set<String> newPseudoKeys = new HashSet<>();
        // fetch all pseudo keys we have seen for this account
        Set<String> fetchedPseudoKeys = getfetchedPseudoKeys(account.getAccountNumber());

        List<TransactionEntity> filteredTransactions =
                rawResponse.getTransactions()
                        .stream()
                        .filter(transaction -> checkIfNewAndStoreTransactionId(transaction, fetchedTransactionIds))
                        .filter(transaction -> checkIfNewAndStorePseudoKey(transaction, fetchedPseudoKeys, newPseudoKeys))
                        .collect(Collectors.toList());

        // add all pseudo keys we have for this batch of transactions
        fetchedPseudoKeys.addAll(newPseudoKeys);

        rawResponse.getTransactions().clear();
        rawResponse.getTransactions().addAll(filteredTransactions);

        return rawResponse;
    }

    // fetch cached transaction ids
    private Set<String> getfetchedTransactionsIds(String accountNumber) {
        if (!transactionIdsSeen.containsKey(accountNumber)) {
            transactionIdsSeen.clear();
            transactionIdsSeen.put(accountNumber, new HashSet<>());
        }

        return transactionIdsSeen.get(accountNumber);
    }

    // fetch cached pseudo keys
    private Set<String> getfetchedPseudoKeys(String accountNumber) {
        if (!pseudoKeysSeen.containsKey(accountNumber)) {
            pseudoKeysSeen.clear();
            pseudoKeysSeen.put(accountNumber, new HashSet<>());
        }

        return pseudoKeysSeen.get(accountNumber);
    }

    // filter transactions to only allow new transaction ids
    // add transaction ids to seenTransaction ids
    private boolean checkIfNewAndStoreTransactionId(TransactionEntity transaction, Set<String> seenTransactionIds) {
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
    private boolean checkIfNewAndStorePseudoKey(TransactionEntity transaction, Set<String> seenKeys, Set<String> newKeys) {

        // if transaction id is set we have decided it is not a duplicate
        if (!Strings.isNullOrEmpty(transaction.getId())) {
            return true;
        }

        String key = transaction.getPseudoKey();
        newKeys.add(key);

        boolean isNewKey = !seenKeys.contains(key);

        return isNewKey;
    }
}
