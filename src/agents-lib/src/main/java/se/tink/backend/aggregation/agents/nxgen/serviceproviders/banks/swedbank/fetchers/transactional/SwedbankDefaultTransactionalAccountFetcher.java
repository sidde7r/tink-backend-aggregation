package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, LinkEntity>, UpcomingTransactionFetcher<TransactionalAccount> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultTransactionalAccountFetcher.class);

    private final SwedbankDefaultApiClient apiClient;
    private final String defaultCurrency;
    private List<String> savingsAccountNumbers;

    private PaymentsConfirmedResponse paymentsConfirmedResponse;

    public SwedbankDefaultTransactionalAccountFetcher(SwedbankDefaultApiClient apiClient, String defaultCurrency) {
        this.apiClient = apiClient;
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ArrayList<TransactionalAccount> accounts = new ArrayList<>();

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
                    // have not found any other way to filter out investment accounts from
                    .filter(account -> getSavingsAccountNumbers().contains(account.getFullyFormattedNumber()))
                    .map(account -> account.toTransactionalAccount(bankProfile))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList()));
            savingsAccountNumbers = null;
        }

        if (apiClient.getBankProfiles().size() > 1) {
            debugLogAccounts(accounts);
        }

        return accounts;
    }

    // DEBUG to see why refresh transactions fails
    private void debugLogAccounts(ArrayList<TransactionalAccount> accounts) {
        try {
            for (TransactionalAccount account : accounts) {
                String uniqueId = account.getUniqueIdentifier();
                BankProfile bankProfile = account.getTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE,
                        BankProfile.class);

                String bankProfileId = "N/A";
                if (bankProfile != null && bankProfile.getBank() != null) {
                    bankProfileId = bankProfile.getBank().getBankId();
                }

                log.info(String.format("Swedbank_multiprofile Account [%s], BankProfileId [%s]",
                        uniqueId, bankProfileId));
            }
        } catch (Exception e) {
            log.warn("Swedbank_multiprofile Failed to log info for multiprofile user");
        }
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        BankProfile bankProfile = account.getTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class);
        apiClient.selectProfile(bankProfile);

        PaymentsConfirmedResponse paymentsConfirmedResponse = apiClient.paymentsConfirmed();

        return paymentsConfirmedResponse.toTinkUpcomingTransactions(account.getAccountNumber());
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {
        BankProfile bankProfile = account.getTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class);
        apiClient.selectProfile(bankProfile);

        if (key != null) {
            return apiClient.engagementTransactions(key);
        }

        LinkEntity nextLink = account.getTemporaryStorage(SwedbankBaseConstants.StorageKey.NEXT_LINK, LinkEntity.class);

        TransactionKeyPaginatorResponseImpl<LinkEntity> transactionKeyPaginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();
        if (nextLink == null) {
            // Return empty response
            return transactionKeyPaginatorResponse;
        }

        // Every time we fetch the transactions for an account we get all reserved transactions.
        // This is a hack to only get the reserved transactions from the first response.
        EngagementTransactionsResponse engagementTransactionsResponse = apiClient.engagementTransactions(nextLink);

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(engagementTransactionsResponse.toTransactions());
        transactions.addAll(engagementTransactionsResponse.reservedTransactionsToTransactions());

        transactionKeyPaginatorResponse.setNext(engagementTransactionsResponse.nextKey());
        transactionKeyPaginatorResponse.setTransactions(transactions);

        return transactionKeyPaginatorResponse;
    }

    // fetch all account number from investment accounts BUT savings accounts, this is because we want savings accounts
    // to be fetched by transactional fetcher to get any transactions
    private List<String> getSavingsAccountNumbers() {

        if (savingsAccountNumbers == null) {
            String portfolioHoldingsString = apiClient.portfolioHoldings();
            PortfolioHoldingsResponse portfolioHoldings = SerializationUtils
                    .deserializeFromString(portfolioHoldingsString, PortfolioHoldingsResponse.class);

            savingsAccountNumbers = portfolioHoldings.getSavingsAccountNumbers();
        }

        return savingsAccountNumbers;
    }
}
