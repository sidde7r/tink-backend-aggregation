package se.tink.backend.aggregation.agents.banks.sbab;

import static se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.INTEGRATION_NAME;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.BankId;
import se.tink.backend.aggregation.agents.banks.sbab.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.IdentityDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.TransferClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.UserDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.configuration.SBABConfiguration;
import se.tink.backend.aggregation.agents.banks.sbab.entities.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.entities.SavedRecipientEntity;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnsupportedTransferException;
import se.tink.backend.aggregation.agents.banks.sbab.executor.SBABTransferExecutor;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;

public class SBABAgent extends AbstractAgent
        implements RefreshTransferDestinationExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor,
                TransferExecutor {

    private final Credentials credentials;
    private final Catalog catalog;

    private final AuthenticationClient authenticationClient;
    private final UserDataClient userDataClient;
    private final TransferClient transferClient;
    private final IdentityDataClient identityDataClient;

    private final Client client;
    private final Client clientWithoutSSL;
    private final LocalDate lastCredentialsUpdate;

    private final SBABTransferExecutor transferExecutor;

    // cache
    private List<AccountEntity> accountEntities = null;

    public SBABAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.catalog = context.getCatalog();
        credentials = request.getCredentials();
        lastCredentialsUpdate =
                credentials.getUpdated() != null
                        ? credentials
                                .getUpdated()
                                .toInstant()
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        : null;

        //        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        //        config.getProperties().put(
        //                ApacheHttpClient4Config.PROPERTY_PROXY_URI,
        //                "http://127.0.0.1:8888"
        //        );
        //       client = clientFactory.createProxyClient(context.getLogOutputStream(), config);
        client = clientFactory.createClientWithRedirectHandler(context.getLogOutputStream());

        HashMap<String, String> payload =
                request.getProvider() == null
                        ? null
                        : SerializationUtils.deserializeFromString(
                                request.getProvider().getPayload(),
                                TypeReferences.MAP_OF_STRING_STRING);

        if (payload != null
                && Objects.equal(
                        payload.get(SBABConstants.IS_MORTGAGE_SWITCH_PROVIDER_TEST),
                        SBABConstants.TRUE)) {
            clientWithoutSSL = clientFactory.createCookieClientWithoutSSL();
        } else {
            clientWithoutSSL = null;
        }

        authenticationClient =
                new AuthenticationClient(client, credentials, CommonHeaders.DEFAULT_USER_AGENT);
        userDataClient = new UserDataClient(client, credentials, CommonHeaders.DEFAULT_USER_AGENT);
        identityDataClient =
                new IdentityDataClient(client, credentials, CommonHeaders.DEFAULT_USER_AGENT);
        transferClient =
                new TransferClient(client, credentials, catalog, CommonHeaders.DEFAULT_USER_AGENT);

        this.transferExecutor =
                new SBABTransferExecutor(transferClient, catalog, supplementalRequester);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        configuration
                .getIntegrations()
                .getIntegration(INTEGRATION_NAME, SBABConfiguration.class)
                .ifPresent(
                        cfg -> {
                            authenticationClient.setConfiguration(cfg);
                            transferClient.setConfiguration(cfg);
                            userDataClient.setConfiguration(cfg);
                        });
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {

        switch (credentials.getType()) {
            case MOBILE_BANKID:
                return Objects.equal(loginWithMobileBankId(), BankIdStatus.DONE);
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - SBAB - Unsupported credential: %s",
                                credentials.getType()));
        }
    }

    private List<AccountEntity> getAccounts() {
        if (accountEntities != null) {
            return accountEntities;
        }

        try {
            accountEntities = userDataClient.getAccounts();
            return accountEntities;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void execute(Transfer transfer) throws Exception, TransferExecutionException {
        switch (transfer.getType()) {
            case BANK_TRANSFER:
                transferExecutor.executeBankTransfer(transfer);
                break;
            default:
                throw new UnsupportedTransferException(transfer.getType());
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception {
        throw new UnsupportedTransferException(transfer.getType());
    }

    private BankIdStatus loginWithMobileBankId() throws BankIdException {
        final InitBankIdResponse initBankIdResponse = authenticationClient.initiateBankIdLogin();
        final String pendingAuthCode = initBankIdResponse.getPendingAuthorizationCode();
        final String autostartToken = initBankIdResponse.getAutostartToken();

        credentials.setSupplementalInformation(null);
        supplementalRequester.openBankId(autostartToken, false);

        for (int i = 0; i < BankId.BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = authenticationClient.getLoginStatus(pendingAuthCode);

            switch (bankIdStatus) {
                case DONE:
                    fetchAndSetBearerToken();
                    return bankIdStatus;
                case CANCELLED:
                    throw BankIdError.CANCELLED.exception();
                case TIMEOUT:
                    throw BankIdError.TIMEOUT.exception();
                case FAILED_UNKNOWN:
                    throw new IllegalStateException("[SBAB - BankId failed with unknown error]");
                default:
                    break;
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    /**
     * Some requests need Bearer authentication. This method makes sure that the clients which need
     * it have it.
     */
    private void fetchAndSetBearerToken() {
        String token = authenticationClient.getBearerToken();
        userDataClient.setBearerToken(token);
        transferClient.setBearerToken(token);
        // store token in the sensitive payload so that it will be masked
        credentials.setSensitivePayload(Key.ACCESS_TOKEN, token);
    }

    private List<Transaction> fetchTransactions(Account account) throws Exception {
        List<Transaction> transactions = Lists.newArrayList();
        String accountNumber = account.getAccountNumber();
        FetchTransactionsResponse response =
                userDataClient.initiateTransactionSearch(accountNumber);

        transactions.addAll(response.getUpcomingTransactions());

        // The initial page contains transactions. If we're already content here, we return.
        if (isContentWithRefresh(account, response.getTransactions())
                || response.getTransactions().isEmpty()) {
            transactions.addAll(response.getTransactions());
            return transactions;
        }

        // Fetch executed transactions until we are content with the refresh.
        boolean hasMoreResults = true;
        int pageNumber = 0;

        while (!isContentWithRefresh(account, transactions) && hasMoreResults) {
            response = userDataClient.fetchTransactions(accountNumber, pageNumber, response);
            transactions.addAll(response.getTransactions());

            hasMoreResults = response.hasMoreResults();
            pageNumber++;
        }

        return transactions;
    }

    private List<Account> toTinkAccounts(List<AccountEntity> sbabAccounts) {
        List<Account> accounts = Lists.newArrayList();

        for (AccountEntity accountEntity : sbabAccounts) {
            Optional<Account> account = accountEntity.toTinkAccount();

            if (account.isPresent()) {
                accounts.add(account.get());
            } else {
                log.error("Could not convert account entity to Tink account");
            }
        }

        return accounts;
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);

        if (clientWithoutSSL != null) {
            filterFactory.addClientFilter(clientWithoutSSL);
        }
    }

    @Override
    public void logout() throws Exception {}

    ///// Refresh Executor Refactor /////

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        try {
            Map<Account, AccountFeatures> accounts = new HashMap<>();
            Map<Account, Loan> loanAccountMapping = userDataClient.getLoans();

            for (Account account : loanAccountMapping.keySet()) {
                Loan loan = loanAccountMapping.get(account);
                accounts.put(account, AccountFeatures.createForLoan(loan));
            }
            return new FetchLoanAccountsResponse(accounts);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public se.tink.backend.aggregation.agents.FetchTransactionsResponse fetchLoanTransactions() {
        return new se.tink.backend.aggregation.agents.FetchTransactionsResponse(
                Collections.emptyMap());
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(toTinkAccounts(getAccounts()));
    }

    @Override
    public se.tink.backend.aggregation.agents.FetchTransactionsResponse fetchSavingsTransactions() {
        try {
            Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
            for (Account account : toTinkAccounts(getAccounts())) {
                List<Transaction> transactions = fetchTransactions(account);
                transactionsMap.put(account, transactions);
            }
            return new se.tink.backend.aggregation.agents.FetchTransactionsResponse(
                    transactionsMap);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        try {
            List<AccountEntity> accountEntities = getAccounts();
            List<SavedRecipientEntity> recipientEntities = transferClient.getSavedRecipients();

            Map<Account, List<TransferDestinationPattern>> transferPatterns =
                    new TransferDestinationPatternBuilder()
                            .setSourceAccounts(accountEntities)
                            .setDestinationAccounts(recipientEntities)
                            .setTinkAccounts(accounts)
                            .addMultiMatchPattern(
                                    AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                            .build();
            return new FetchTransferDestinationsResponse(transferPatterns);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataClient.fetchIdentityData());
    }

    /////////////////////////////////////
}
