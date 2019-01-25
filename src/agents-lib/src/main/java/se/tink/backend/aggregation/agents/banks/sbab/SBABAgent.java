package se.tink.backend.aggregation.agents.banks.sbab;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.util.Precision;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdMessage;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.sbab.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.BankIdSignClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.TransferClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.UserDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnsupportedTransferException;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.BankIdStartResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InitialTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MakeTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SavedRecipientEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.configuration.integrations.SbabConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.DocumentIdentifier;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SBABAgent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor {

    private final Credentials credentials;
    private final Catalog catalog;

    private static final int BANKID_MAX_ATTEMPTS = 100;

    private final AuthenticationClient authenticationClient;
    private final UserDataClient userDataClient;
    private final TransferClient transferClient;
    private final BankIdSignClient bankIdSignClient;
    private final Client client;
    private final Client clientWithoutSSL;
    private final LocalDate lastCredentialsUpdate;

    // cache
    private List<AccountEntity> accountEntities = null;

    public SBABAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);
        this.catalog = context.getCatalog();
        credentials = request.getCredentials();
        lastCredentialsUpdate = credentials.getUpdated() != null ?
                credentials.getUpdated().toInstant().atZone(ZoneId.of("UTC")).toLocalDate() : null;
        client = clientFactory.createClientWithRedirectHandler(context.getLogOutputStream());

        HashMap<String, String> payload = request.getProvider() == null ? null : SerializationUtils
                .deserializeFromString(request.getProvider().getPayload(), TypeReferences.MAP_OF_STRING_STRING);

        if (payload != null && Objects.equal(payload.get("isSwitchMortgageProviderTest"), "true")) {
            clientWithoutSSL = clientFactory.createCookieClientWithoutSSL();
            bankIdSignClient = new BankIdSignClient(clientWithoutSSL, credentials, DEFAULT_USER_AGENT);
        } else {
            clientWithoutSSL = null;
            bankIdSignClient = new BankIdSignClient(client, credentials, DEFAULT_USER_AGENT);
        }

        authenticationClient = new AuthenticationClient(client, credentials, DEFAULT_USER_AGENT);
        userDataClient = new UserDataClient(client, credentials, DEFAULT_USER_AGENT);
        transferClient = new TransferClient(client, credentials, catalog, DEFAULT_USER_AGENT);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        SbabConfiguration sbabConfiguration = configuration.getIntegrations().getSbab();

        if (sbabConfiguration != null) {
            authenticationClient.setConfiguration(sbabConfiguration);
            bankIdSignClient.setConfiguration(sbabConfiguration);
            transferClient.setConfiguration(sbabConfiguration);
            userDataClient.setConfiguration(sbabConfiguration);
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        switch (credentials.getType()) {
        case MOBILE_BANKID:
            return Objects.equal(loginWithMobileBankId(), BankIdStatus.DONE);
        default:
            throw new IllegalStateException(
                    String.format("#login-refactoring - SBAB - Unsupported credential: %s", credentials.getType()));
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
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            // nop
            break;

        case TRANSFER_DESTINATIONS:
            try {
                updateTransferDestinations();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case SAVING_ACCOUNTS:
            updateAccounts();
            break;

        case SAVING_TRANSACTIONS:
            try {
                for (Account account : toTinkAccounts(getAccounts())) {
                    List<Transaction> transactions = fetchTransactions(account);
                    financialDataCacher.updateTransactions(account, transactions);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case LOAN_ACCOUNTS:
            try {
                updateLoans();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        }
    }

    @Override
    public void execute(Transfer transfer) throws Exception, TransferExecutionException {
        switch (transfer.getType()) {
        case BANK_TRANSFER:
            executeBankTransfer(transfer);
            break;
        default:
            throw new UnsupportedTransferException(transfer.getType());
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {
        throw new UnsupportedTransferException(transfer.getType());
    }

    private void executeBankTransfer(Transfer transfer) throws Exception {
        InitialTransferResponse initialResponse = transferClient.initiateProcess();
        Optional<String> sourceAccount = transferClient.tryFindSourceAccount(transfer, initialResponse);

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not find the source account number in the list from the bank. New format?")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        if (!accountIsSolvent(sourceAccount.get(), transfer)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Amount is larger than what is available at the account.")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                    .build();
        }

        Optional<SavedRecipientEntity> recipient = transferClient.tryFindRecipient(transfer, initialResponse);
        MakeTransferResponse makeResponse = transferClient.makeTransfer(transfer, recipient, initialResponse);

        try {
            if (makeResponse.isBetweenUserAccounts()) {
                transferClient.acceptTransfer(makeResponse);
            } else {
                signTransfer(makeResponse);
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            transferClient.deleteTransfer(makeResponse);
            throw e;
        }
    }

    private boolean accountIsSolvent(final String sourceAccountNumber, Transfer transfer) throws Exception {
        // Fetch accounts to get the current balance of the accounts
        List<Account> accounts = toTinkAccounts(getAccounts());
        ImmutableList<Account> account = FluentIterable.from(accounts).filter(
                account1 -> Objects.equal(account1.getBankId(), sourceAccountNumber)).toList();

        Account sourceAccount = Iterables.getOnlyElement(account);
        return Precision.compareTo(sourceAccount.getBalance(), transfer.getAmount().getValue(), 0.001) >= 0;
    }

    private void signTransfer(MakeTransferResponse makeResponse) throws Exception {
        SignFormRequestBody signFormRequestBody = transferClient.initiateSignProcess(makeResponse);

        BankIdStatus bankIdStatus = signWithMobileBankId(signFormRequestBody);

        switch (bankIdStatus) {
        case DONE:
            transferClient.checkTransferSuccess(makeResponse, true);
            log.info("Successfully signed transfer");
            return;
        case CANCELLED:
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString(BankIdMessage.BANKID_CANCELLED)).build();
        case TIMEOUT:
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString(BankIdMessage.BANKID_NO_RESPONSE)).build();
        case FAILED_UNKNOWN:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED).build();
        default:
            break;
        }
    }

    private BankIdStatus loginWithMobileBankId() throws BankIdException {
        authenticationClient.initiateBankIdLogin();

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        supplementalRequester.requestSupplementalInformation(credentials, false);

        for (int i = 0; i < BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = authenticationClient.getLoginStatus();

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
     * Some requests need Bearer authentication. This method makes sure that the clients which need it have it.
     */
    private void fetchAndSetBearerToken() {
        String token = authenticationClient.getBearerToken();
        userDataClient.setBearerToken(token);
    }

    private BankIdStatus signWithMobileBankId(SignFormRequestBody signFormRequestBody) throws Exception {
        BankIdStartResponse startResponse = bankIdSignClient.initiateSign(signFormRequestBody);

        requestBankIdSupplemental();

        for (int i = 0; i < BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = bankIdSignClient.getStatus(signFormRequestBody, startResponse.getOrderRef());

            if (!Objects.equal(bankIdStatus, BankIdStatus.WAITING)) {
                return bankIdStatus;
            }

            Thread.sleep(2000);
        }

        return BankIdStatus.TIMEOUT;
    }

    private List<Transaction> fetchTransactions(Account account) throws Exception {
        List<Transaction> transactions = Lists.newArrayList();
        String accountNumber = account.getAccountNumber();
        FetchTransactionsResponse response = userDataClient.initiateTransactionSearch(accountNumber);

        transactions.addAll(response.getUpcomingTransactions());

        // The initial page contains transactions. If we're already content here, we return.
        if (isContentWithRefresh(account, response.getTransactions()) || response.getTransactions().isEmpty()) {
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

    private void updateAccounts() {
        financialDataCacher.cacheAccounts(toTinkAccounts(getAccounts()));
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

    private void updateLoans() throws Exception {
        Map<Account, Loan> loanAccountMapping = userDataClient.getLoans();
        boolean updateAmortizationDocument = isUpdateAmortizationDocument();

        for (Account account : loanAccountMapping.keySet()) {
            Loan loan = loanAccountMapping.get(account);
            financialDataCacher.cacheAccount(account, AccountFeatures.createForLoan(loan));

            if (updateAmortizationDocument) {
                systemUpdater.updateDocument(getAmortizationDocumentation(loan.getLoanNumber()));
            }
        }
    }

    private DocumentContainer getAmortizationDocumentation(String loanNumber) throws Exception {
        DocumentContainer amortizationDocumentation = userDataClient.getAmortizationDocumentation(loanNumber);
        String identifier = DocumentIdentifier.AMORTIZATION_DOCUMENTATION + "-" + loanNumber;
        amortizationDocumentation.setIdentifier(identifier);

        return amortizationDocumentation;
    }

    private boolean isUpdateAmortizationDocument() throws UnsupportedEncodingException {
        if (lastCredentialsUpdate == null) {
            return true;
        }

        // Hash the credentials id and take modulus 28 to get an integer in the range [0, 27].
        // This will be the number of days added to the first day of the month to get the date
        // that we will update the amortization documentation for this credential.
        // This is done to not download the documentation for all users the same day.
        // The number 27 is chosen with respect to that february only have 28 days in a none leap year.
        int dayOfMonth = Hashing.murmur3_32()
                .hashString(credentials.getId().replace("-", ""), Charset.forName("UTF-8"))
                .asInt() % 28;

        LocalDate now = LocalDate.now();
        LocalDate dateToUpdateAmortizationDocument = lastCredentialsUpdate.withDayOfMonth(1).plusDays(dayOfMonth);

        return now.isAfter(dateToUpdateAmortizationDocument);
    }


    public void updateTransferDestinations() throws Exception {
        List<AccountEntity> accountEntities = getAccounts();
        List<SavedRecipientEntity> recipientEntities = transferClient.getValidRecipients();

        Map<Account, List<TransferDestinationPattern>> transferPatterns = new TransferDestinationPatternBuilder()
                .setSourceAccounts(accountEntities)
                .setDestinationAccounts(recipientEntities)
                .setTinkAccounts(systemUpdater.getUpdatedAccounts())
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();

        systemUpdater.updateTransferDestinationPatterns(transferPatterns);
    }

    private void requestBankIdSupplemental() {
        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);

        if (clientWithoutSSL != null) {
            filterFactory.addClientFilter(clientWithoutSSL);
        }
    }

    @Override
    public void logout() throws Exception {
    }
}
