package se.tink.backend.aggregation.agents.banks.sbab;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;
import org.apache.commons.math3.util.Precision;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdMessage;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.UnsupportedApplicationException;
import se.tink.backend.aggregation.agents.banks.sbab.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.BankIdSignClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.MortgageClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.MortgageSignClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.OpenSavingsAccountClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.TransferClient;
import se.tink.backend.aggregation.agents.banks.sbab.client.UserDataClient;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnacceptedTermsAndConditionsException;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnsupportedTransferException;
import se.tink.backend.aggregation.agents.banks.sbab.model.request.MortgageApplicationRequest;
import se.tink.backend.aggregation.agents.banks.sbab.model.request.MortgageSignatureRequest;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.BankIdStartResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.DiscountResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InitialTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InterestRateEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InterestsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MakeTransferResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageSignatureStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.MortgageStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.OpenSavingsAccountResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SavedRecipientEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.utils.CreateProductExecutorTracker;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.CreateProductResponse;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.common.config.SbabIntegrationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.DocumentIdentifier;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.utils.Doubles;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SBABAgent extends AbstractAgent implements RefreshableItemExecutor, CreateProductExecutor,
        TransferExecutor {

    private final Credentials credentials;
    private final Catalog catalog;

    private static final int BANKID_MAX_ATTEMPTS = 100;

    private final MortgageClient mortgageClient;
    private final MortgageSignClient mortgageSignClient;
    private final AuthenticationClient authenticationClient;
    private final UserDataClient userDataClient;
    private final TransferClient transferClient;
    private final BankIdSignClient bankIdSignClient;
    private final Client client;
    private final OpenSavingsAccountClient openSavingsAccountClient;
    private final Client clientWithoutSSL;
    private final LocalDate lastCredentialsUpdate;

    // cache
    private List<AccountEntity> accountEntities = null;

    public SBABAgent(CredentialsRequest request, AgentContext context) {
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
            mortgageSignClient = new MortgageSignClient(clientWithoutSSL, credentials);
            bankIdSignClient = new BankIdSignClient(clientWithoutSSL, credentials);
        } else {
            clientWithoutSSL = null;
            mortgageSignClient = new MortgageSignClient(client, credentials);
            bankIdSignClient = new BankIdSignClient(client, credentials);
        }

        mortgageClient = new MortgageClient(client, credentials,
                new CreateProductExecutorTracker(context.getMetricRegistry()));
        authenticationClient = new AuthenticationClient(client, credentials);
        userDataClient = new UserDataClient(client, credentials);
        transferClient = new TransferClient(client, credentials, catalog);
        openSavingsAccountClient = new OpenSavingsAccountClient(client, credentials);
    }

    @Override
    public void setConfiguration(ServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        SbabIntegrationConfiguration sbabConfiguration = configuration.getIntegrations().getSbab();

        if (sbabConfiguration != null) {
            authenticationClient.setConfiguration(sbabConfiguration);
            bankIdSignClient.setConfiguration(sbabConfiguration);
            mortgageClient.setConfiguration(sbabConfiguration);
            mortgageSignClient.setConfiguration(sbabConfiguration);
            openSavingsAccountClient.setConfiguration(sbabConfiguration);
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
                    context.updateTransactions(account, transactions);
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


    @Override
    public CreateProductResponse create(GenericApplication application) throws Exception {
        switch (application.getType()) {
        case SWITCH_MORTGAGE_PROVIDER:
            return new CreateProductResponse(switchMortgageProvider(application));
        case OPEN_SAVINGS_ACCOUNT:
            return new CreateProductResponse(openSavingsAccount(application));
        default:
            throw new UnsupportedApplicationException(application.getType());
        }
    }

    @Override
    public void fetchProductInformation(ProductType type, UUID productInstanceId,
            Map<FetchProductInformationParameterKey, Object> parameters) {

        if (!Objects.equal(ProductType.MORTGAGE, type)) {
            log.warn(String.format("Product information can't be fetched for product type '%s'.", type));
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.MARKET_VALUE)) {
            log.error(String.format("[productInstanceId:%s] Market value is missing.", productInstanceId));
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.MORTGAGE_AMOUNT)) {
            log.error(String.format("[productInstanceId:%s] Mortgage amount is missing.", productInstanceId));
            return;
        }

        if (!parameters.containsKey(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS)) {
            log.error(String.format("[productInstanceId:%s] Number of applicants is missing.", productInstanceId));
            return;
        }

        int marketValue = (int) parameters.get(FetchProductInformationParameterKey.MARKET_VALUE);
        int mortgageAmount = (int) parameters.get(FetchProductInformationParameterKey.MORTGAGE_AMOUNT);
        int numberOfApplicants = (int) parameters.get(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS);

        try {
            InterestsResponse interestResponse = mortgageClient.getInterestRates(marketValue, mortgageAmount);

            // Find the 3 months duration rate entity.
            InterestRateEntity rate = Iterables.find(interestResponse.getInterestRates(),
                    rateEntity -> Doubles.fuzzyEquals(rateEntity.getContractDurationInMonths(), 3d, 0.1));

            DiscountResponse discountResponse = mortgageClient.getMortgageDiscounts(numberOfApplicants, mortgageAmount,
                    "BYT_BANK");

            HashMap<ProductPropertyKey, Object> properties = new HashMap<>();
            properties.put(ProductPropertyKey.INTEREST_RATE, rate.getCustomerRate() / 100);
            properties.put(ProductPropertyKey.LIST_INTEREST_RATE, rate.getListRate() / 100);
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT, discountResponse.getDiscount() / 100);
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT_DESCRIPTION, discountResponse.getDescription());
            properties.put(ProductPropertyKey.INTEREST_RATE_DISCOUNT_DURATION_MONTHS,
                    discountResponse.getNumberOfMonths());

            log.debug(String.format(
                    "[productInstanceId:%s, marketValue:%d, mortgageAmount:%d, numberOfApplicants:%d] %s.",
                    productInstanceId, marketValue, mortgageAmount, numberOfApplicants, properties));

            context.updateProductInformation(productInstanceId, properties);
        } catch (NoSuchElementException e) {
            log.error(
                    String.format(
                            "No interest rate with 3 months duration available [productInstanceId:%s, marketValue:%d, mortgageAmount:%d, numberOfApplicants:%d].",
                            productInstanceId, marketValue, mortgageAmount, numberOfApplicants));
        } catch (Exception e) {
            log.error(String.format("[productInstanceId:%s] Unable to fetch product information.", productInstanceId), e);
        }
    }

    @Override
    public void refreshApplication(ProductType type, UUID applicationId,
            Map<RefreshApplicationParameterKey, Object> parameters) throws Exception {
        String externalId = getExternalId(parameters);

        Preconditions.checkState(!Strings.isNullOrEmpty(externalId),
                "No external application reference was supplied.");

        ApplicationState applicationState = null;

        try {
            MortgageStatus mortgageStatus = mortgageClient.getMortgageStatus(externalId);
            applicationState = createApplicationState(mortgageStatus);
        } catch (UniformInterfaceException e) {
            if (Objects.equal(e.getResponse().getStatus(), Response.Status.NOT_FOUND.getStatusCode())) {
                applicationState = new ApplicationState();
                applicationState.setNewApplicationStatus(ApplicationStatusKey.EXPIRED);
            } else {
                throw e;
            }
        }
        context.updateApplication(applicationId, applicationState);
    }

    private String getExternalId(Map<RefreshApplicationParameterKey, Object> parameters) {
        Object parameter = parameters.get(RefreshApplicationParameterKey.EXTERNAL_ID);
        return parameter != null ? String.valueOf(parameter) : null;
    }

    private ApplicationState createApplicationState(MortgageStatus mortgageStatus) {
        Preconditions.checkArgument(mortgageStatus != null,
                "Mortgage status not available.");

        ApplicationState applicationState = new ApplicationState();

        ApplicationStatusKey statusKey = getApplicationStatus(mortgageStatus);
        applicationState.setNewApplicationStatus(statusKey);
        applicationState.setApplicationProperty(ApplicationPropertyKey.EXTERNAL_STATUS, mortgageStatus.name());

        return applicationState;
    }

    private ApplicationStatusKey getApplicationStatus(MortgageStatus mortgageStatus) {
        switch (mortgageStatus) {
        case MAKULERAD:
            return ApplicationStatusKey.ABORTED;
        case UTBETALT:
            return ApplicationStatusKey.EXECUTED;
        case AVSLAGEN:
            return ApplicationStatusKey.REJECTED;
        case TEKNISKT_FEL:
            return ApplicationStatusKey.ERROR;
        case AVSLAGEN_UC:
        case BEARBETNING_PAGAR:
        case ANSOKAN_REGISTRERAD:
            return ApplicationStatusKey.SIGNED;
        case KOMPLETTERING_KRAVS:
            return ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED;
        case LANEHANDLINGAR_KLARA:
        case LANEHANDLINGAR_INKOMNA:
            return ApplicationStatusKey.APPROVED;
        default:
            throw new IllegalStateException(String.format(
                    "The mortgage status '%s' is not mapped to an application status.", mortgageStatus.name()));
        }
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
        context.requestSupplementalInformation(credentials, false);

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

    private BankIdStatus signMortgageWithMobileBankId(SignFormRequestBody signFormRequestBody) throws Exception {
        BankIdStartResponse startResponse = mortgageSignClient.initiateSign(signFormRequestBody);

        requestBankIdSupplemental();

        for (int i = 0; i < BANKID_MAX_ATTEMPTS; i++) {
            BankIdStatus bankIdStatus = mortgageSignClient.getStatus(signFormRequestBody, startResponse.getOrderRef());

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
        context.updateAccounts(toTinkAccounts(getAccounts()));
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
            context.updateAccount(account, AccountFeatures.createForLoan(loan));

            if (updateAmortizationDocument) {
                context.updateDocument(getAmortizationDocumentation(loan.getLoanNumber()));
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
                .setTinkAccounts(context.getAccounts())
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();

        context.updateTransferDestinationPatterns(transferPatterns);
    }

    private String switchMortgageProvider(GenericApplication application) throws Exception {
        // Create the request objects before calling SBAB api's so that we bail early if models contains errors
        MortgageSignatureRequest signatureRequest = mortgageClient.getSignatureRequest(application);
        MortgageApplicationRequest mortgageApplicationRequest = mortgageClient
                .getMortgageApplicationRequest(application);

        mortgageClient.setRemoteIp(application.getRemoteIp());

        String signatureId = mortgageClient.createSignature(signatureRequest);
        signMortgageSignature(signatureId);

        return mortgageClient.sendApplication(mortgageApplicationRequest, signatureId);
    }

    private void signMortgageSignature(String signatureId) throws Exception {
        SignFormRequestBody signFormRequestBody = mortgageSignClient.initiateSignProcess(signatureId);

        BankIdStatus bankIdStatus = signMortgageWithMobileBankId(signFormRequestBody);

        switch (bankIdStatus) {
        case DONE:
            MortgageSignatureStatus finalStatus = mortgageClient.getMortgageSigningStatus(signatureId);

            if (Objects.equal(finalStatus, MortgageSignatureStatus.SUCCESSFUL)) {
                log.info("Successfully created and signed a new mortgage application signature.");
                return;
            } else {
                throw new IllegalStateException(
                        String.format("[BankIdStatus: %s, MortgageSignatureStatus: %s]", bankIdStatus, finalStatus));
            }
        case CANCELLED:
            throw BankIdError.CANCELLED.exception();
        case TIMEOUT:
            throw BankIdError.TIMEOUT.exception();
        case FAILED_UNKNOWN:
        default:
            throw new IllegalStateException(String.format("[BankIdStatus: %s]", bankIdStatus));
        }
    }

    private void requestBankIdSupplemental() {
        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);
        context.requestSupplementalInformation(credentials, false);
    }

    private String openSavingsAccount(GenericApplication application) throws Exception {
        openSavingsAccountClient.setRemoteIp(application.getRemoteIp());

        // The user must first login at SBAB, even if the user doesn't have an account there yet.
        BankIdStatus bankIdStatus = loginWithMobileBankId();

        switch (bankIdStatus) {
        case DONE:
            break;
        case CANCELLED:
            throw BankIdError.CANCELLED.exception();
        case TIMEOUT:
            throw BankIdError.TIMEOUT.exception();
        case FAILED_UNKNOWN:
        default:
            throw new IllegalStateException(String.format("[BankId status]: %s", bankIdStatus));
        }

        List<AccountEntity> accountsBefore;

        log.info("Login completed.");

        try {
            accountsBefore = userDataClient.getAccounts();
        } catch (UnacceptedTermsAndConditionsException e) {
            log.info("User has not accepted Terms & Conditions. Initiating signing.");
            // The user has not accepted the Terms & Conditions for getting access to SBABs services. These needs
            // to be accepted before we can retrieve accounts and open new savings accounts.
            SignFormRequestBody signForm = userDataClient.initiateTermsAndConditionsSigning(e.getUrl(), e.getInput());

            signTermsAndConditions(signForm);

            log.info("User accepted Terms & Conditions.");

            accountsBefore = userDataClient.getAccounts();

            log.info(String.format("Fetched %d accounts.", accountsBefore.size()));
        }

        OpenSavingsAccountResponse response = openSavingsAccountClient.submit(application);

        return signNewSavingsAccount(response, accountsBefore);
    }

    private String signNewSavingsAccount(OpenSavingsAccountResponse openAccountResponse,
            List<AccountEntity> accountsBefore) throws Exception {

        SignFormRequestBody signFormRequestBody = openSavingsAccountClient.initiateSignProcess(openAccountResponse);

        BankIdStatus bankIdStatus = signWithMobileBankId(signFormRequestBody);

        switch (bankIdStatus) {
        case DONE:
            List<AccountEntity> accountsAfter = userDataClient.getAccounts();
            String accountNumber = openSavingsAccountClient.getNewAccountNumber(accountsBefore, accountsAfter);
            log.info(String.format("Successfully created a new savings account (account number %s).", accountNumber));
            // TODO: This should probably be done by a separate refresh call instead, but for some reason that doesn't
            // work.
            context.updateAccounts(toTinkAccounts(accountsAfter));
            return accountNumber;
        case CANCELLED:
            throw BankIdError.CANCELLED.exception();
        case TIMEOUT:
            throw BankIdError.TIMEOUT.exception();
        case FAILED_UNKNOWN:
        default:
            throw new IllegalStateException(String.format("[BankId status]: %s", bankIdStatus));
        }
    }

    private void signTermsAndConditions(SignFormRequestBody signFormRequestBody) throws Exception {
        BankIdStatus bankIdStatus = signWithMobileBankId(signFormRequestBody);

        switch (bankIdStatus) {
        case DONE:
            return;
        case CANCELLED:
            throw BankIdError.CANCELLED.exception();
        case TIMEOUT:
            throw BankIdError.TIMEOUT.exception();
        case FAILED_UNKNOWN:
        default:
            throw new IllegalStateException(String.format("[BankId status]: %s", bankIdStatus));
        }
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
