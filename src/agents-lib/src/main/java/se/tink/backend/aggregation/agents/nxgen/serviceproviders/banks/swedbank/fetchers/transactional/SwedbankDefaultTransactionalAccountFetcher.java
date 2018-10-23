package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, LinkEntity>, UpcomingTransactionFetcher<TransactionalAccount> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultTransactionalAccountFetcher.class);

    private final SwedbankDefaultApiClient apiClient;
    private List<String> investmentAccountNumbers;
    private Date earliestDateSeen;

    public SwedbankDefaultTransactionalAccountFetcher(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
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

    // 2018-10-17: Swedbank has had a problem where they hand us the same page multiple times during pagination.
    // We solve this on our end by comparing the earliest date in each batch and compare the saved date to
    // see if we have encountered the page before.
    private void updateEarliestDateSeen(TransactionKeyPaginatorResponse<LinkEntity> response) {
        Optional<? extends Transaction> earliestTransaction = response.getTinkTransactions()
                .stream()
                .min(Comparator.comparing(Transaction::getDate));

        earliestTransaction.ifPresent(transaction -> earliestDateSeen = transaction.getDate());
    }

    private boolean hasSeenPageBefore(TransactionKeyPaginatorResponse<LinkEntity> response) {
        return response.getTinkTransactions()
                .stream()
                .min(Comparator.comparing(Transaction::getDate))
                .filter(trans -> !trans.getDate().before(earliestDateSeen))
                .isPresent();
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {
        BankProfile bankProfile =
                account.getFromTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                        .orElseThrow(() -> new IllegalStateException("No bank profile specified"));
        apiClient.selectProfile(bankProfile);

        if (key != null) {
            TransactionKeyPaginatorResponse<LinkEntity> response;

            // Swedbank sometimes responds with 500 in the middle of the pagination. Return an empty response
            // with key as null to quit the paginating and return the transactions that we managed to fetch so far.
            try {
                response = apiClient.engagementTransactions(key);
            } catch (HttpResponseException e) {

                HttpResponse httpResponse = e.getResponse();

                if (httpResponse != null && httpResponse.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    log.warn("Got internal server error when paginating transactions.");
                    return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
                }

                throw e;
            }

            if (hasSeenPageBefore(response)) {
                // Return an empty response but with the correct next key set.
                return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), response.nextKey());
            }

            // Only update the earliestDateSeen if we haven't seen the page before.
            updateEarliestDateSeen(response);
            return response;
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
        EngagementTransactionsResponse engagementTransactionsResponse = apiClient.engagementTransactions(nextLink);

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(engagementTransactionsResponse.toTransactions());
        transactions.addAll(engagementTransactionsResponse.reservedTransactionsToTransactions());

        transactionKeyPaginatorResponse.setNext(engagementTransactionsResponse.nextKey());
        transactionKeyPaginatorResponse.setTransactions(transactions);

        updateEarliestDateSeen(transactionKeyPaginatorResponse);

        return transactionKeyPaginatorResponse;
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
}
