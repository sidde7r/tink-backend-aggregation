package se.tink.backend.aggregation.agents.banks.se.icabanken;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.icabanken.IcaBankenAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AcceptEInvoiceTransferRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AssignmentsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.AssignmentsResponseBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.BankEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.BankTransferRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.BanksResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.CollectBankIdResponseBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.DepotEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.DepotsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EInvoiceEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EInvoiceResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EndBankIdAuthenticationRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EngagementResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EngagementResponseBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.FundDetails;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.FundHoldingsEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.FundsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.InitSignRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.LoanEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.LoanListEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.LoansResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.LoansResponseBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.MortgageEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.MortgageListEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.OwnRecipientEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.PaymentNameResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.RecipientEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.RecipientsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.Response;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.SessionResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.SessionResponseBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.SignBundleResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.SignedAssignmentList;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.TransferRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.TransferResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.UpcomingTransactionsBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.UpdateEInvoiceRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.ValidateEInvoiceRequest;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.agents.banks.se.icabanken.types.IcaDestinationType;
import se.tink.backend.aggregation.agents.banks.se.icabanken.types.IcaSourceType;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
<<<<<<< HEAD
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
=======
import se.tink.backend.aggregation.rpc.CredentialsRequest;
>>>>>>> refactor(ica-refresher) ica banken refresher refactor
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.TransactionOrdering;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ICABankenAgent extends AbstractAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                TransferExecutor,
                PersistentLogin {
    // Do not change, will require migration
    private static final String DEVICEAPPLICATIONID_KEY = "deviceApplicationId";

    private static final String BASE_URL = "https://appserver.icabanken.se";
    private static final String SESSION_URL = BASE_URL + "/api/session?deviceApplicationId=%s";
    private static final String HEARTBEAT_URL = BASE_URL + "/api/session/heartbeat";
    private static final String ENGAGEMENTS_URL = BASE_URL + "/api/engagement";
    private static final String INIT_BANKID_LOGIN_URL = BASE_URL + "/api/session/login/bankid/%s";
    private static final String INIT_TRANSFER_SIGN_URL =
            BASE_URL + "/api/assignments/bundle/bankid/init";
    private static final String INIT_EINVOICE_SIGN_URL =
            BASE_URL + "/api/egiro/recipient/bankId/init/%s";
    private static final String AUTHENTICATE_COLLECT_URL =
            BASE_URL + "/api/session/login/bankid/%s";
    private static final String SIGN_TRANSFER_COLLECT_URL =
            BASE_URL + "/api/bankId/sign/collect/%s";
    private static final String SIGNED_ASSIGNMENTS_URL =
            BASE_URL + "/api/assignments/bundle/bankid/submit?requestId=%s";
    private static final String UNSIGNED_ASSIGNMENTS_URL = BASE_URL + "/api/assignments";
    private static final String UPCOMING_TRANSACTIONS_URL = BASE_URL + "/api/events/future";
    private static final String TRANSFER_DESTINATIONS_URL = BASE_URL + "/api/recipients";
    private static final String UNSIGNED_TRANSFERS_URL = BASE_URL + "/api/events/unsigned";
    private static final String ACCOUNTS_URL = BASE_URL + "/api/accounts";
    private static final String RESERVED_TRANSACTIONS_URL =
            BASE_URL + "/api/accounts/%s/reservedTransactions";
    private static final String TRANSACTIONS_URL =
            BASE_URL + "/api/accounts/%s/transactions?toDate=%s";
    private static final String EINVOICES_URL = BASE_URL + "/api/egiro/invoices";
    private static final String DELETE_UNSIGNED_TRANSFER_URL =
            BASE_URL + "/api/assignments/bundle/%s";
    private static final String TRANSFER_BANKS_URL = BASE_URL + "/api/accounts/transferBanks";
    private static final String GIRO_DESTINATION_NAME =
            BASE_URL + "/api/recipients/pgBgRecipientName/%s";
    private static final String END_BANKID_AUTHENTICATION_URL =
            BASE_URL + "/api/egiro/recipient/bankId";
    private static final String ACCEPT_EINVOICE_URL = BASE_URL + "/api/egiro/invoice/accept";
    private static final String VALIDATE_INVOICE_URL = BASE_URL + "/api/egiro/invoice/validate";
    private static final String UPDATE_INVOICE_URL = BASE_URL + "/api/egiro/invoice/update";
    private static final String LOANS_URL = BASE_URL + "/api/engagement/loans";
    private static final String LOGOUT_URL = BASE_URL + "/api/session/logout";
    private static final String DEPOTS_URL = BASE_URL + "/api/depots";
    private static final String FUNDS_URL = BASE_URL + "/api/funds/%s";

    private static final String API_KEY = "BBA7D74A-042F-4E87-8C88-55412320F186";
    private static final String API_VERSION = "8";
    private static final String CLIENT_APP_VERSION = "1.45.1";
    private static final String CLIENT_HARDWARE = "Nexus 5X";
    private static final String CLIENT_OS = "Android";
    private static final String CLIENT_OS_VERSION = "23";
    private static final int MAX_ATTEMPTS = 90;
    private static final ThreadSafeDateFormat DATE_FORMAT = ThreadSafeDateFormat.FORMATTER_DAILY;
    private static final AccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new IcaBankenAccountIdentifierFormatter();
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG =
            TransferMessageLengthConfig.createWithMaxLength(25, 12, 25);
    private static final Retryer<ValidateEInvoiceResponse>
            WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER =
                    RetryerBuilder.<ValidateEInvoiceResponse>newBuilder()
                            .retryIfResult(
                                    ValidateEInvoiceResponse::isInvalidButICABankenCorrectedIt)
                            // Upper retry bound is important to avoid infinite loop.
                            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                            .build();
    private final TinkApacheHttpClient4 client;
    private final Credentials credentials;
    private final TransferMessageFormatter transferMessageFormatter;
    private final Catalog catalog;
    private String sessionId;
    private SessionResponseBody sessionResponse;
    private String userInstallationId;

    // cache
    private List<AccountEntity> accountEntities = null;

    public ICABankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.catalog = context.getCatalog();
        this.credentials = request.getCredentials();

        this.client = clientFactory.createCustomClient(context.getLogOutputStream());
        // client.addFilter(new LoggingFilter(new PrintStream(System.out)));
        this.transferMessageFormatter =
                new TransferMessageFormatter(
                        context.getCatalog(),
                        TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(".-?!/+%"));
    }

    private static Transfer getOriginalTransfer(Transfer transfer) {
        return transfer.getOriginalTransfer()
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(
                                                "No original transfer on payload to compare with.")
                                        .build());
    }

    private Optional<String> fetchDestinationNameFor(String giroNumber) {
        PaymentNameResponse paymentNameResponse =
                createClientRequest(String.format(GIRO_DESTINATION_NAME, giroNumber))
                        .type(MediaType.APPLICATION_JSON)
                        .get(PaymentNameResponse.class);

        return paymentNameResponse.getBody().getName();
    }

    private String findDestinationNameFor(final AccountIdentifier destination) {
        Optional<String> destinationName = destination.getName();

        if (destinationName.isPresent()) {
            return destinationName.get();
        }

        if (!Objects.equal(destination.getType(), AccountIdentifier.Type.SE)) {
            destinationName =
                    fetchDestinationNameFor(
                            destination.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER));
        }

        return destinationName.orElseGet(this::requestSupplementalDestinationName);
    }

    private String fetchBankIdFor(final AccountIdentifier destination) {
        BanksResponse banksResponse =
                createClientRequest(TRANSFER_BANKS_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .get(BanksResponse.class);

        Optional<BankEntity> bankEntity =
                ICABankenUtils.findBankForAccountNumber(
                        destination.getIdentifier(DEFAULT_FORMATTER),
                        banksResponse.getBody().getTransferBanks());

        if (!bankEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            context.getCatalog()
                                    .getString(
                                            "Could not find a bank for the given destination account. Check the account number and try again."))
                    .build();
        }

        return bankEntity.get().getTransferBankId();
    }

    private Optional<RecipientEntity> addDestination(final AccountIdentifier destination) {
        String transferType =
                ICABankenUtils.identifierTypeToString(destination.getType(), context.getCatalog());

        // Create the new recipient.
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setAccountNumber(destination.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER));
        recipientEntity.setType(transferType);
        recipientEntity.setName(findDestinationNameFor(destination));
        recipientEntity.setBudgetGroup("");

        if (Objects.equal(destination.getType(), AccountIdentifier.Type.SE)) {
            String bankId = fetchBankIdFor(destination);

            recipientEntity.setTransferBankId(bankId);
        }

        createClientRequest(TRANSFER_DESTINATIONS_URL)
                .type(MediaType.APPLICATION_JSON)
                .post(RecipientsResponse.class, recipientEntity);

        // Get find destination again
        return tryFindRegisteredDestinationAccount(destination);
    }

    private String requestSupplementalDestinationName() {
        Field nameField = new Field();

        nameField.setDescription("Mottagarnamn");
        nameField.setName("name");

        List<Field> fields = Lists.newArrayList(nameField);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation =
                supplementalRequester.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        if (!Strings.isNullOrEmpty(supplementalInformation)) {
            Map<String, String> answers =
                    SerializationUtils.deserializeFromString(
                            supplementalInformation,
                            new TypeReference<HashMap<String, String>>() {});

            String destinationName = answers.get("name");
            if (!Strings.isNullOrEmpty(destinationName)) {
                return destinationName;
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        context.getCatalog().getString("Could not get recipient name from user"))
                .build();
    }

    /** Authenticate the user with Mobile BankID. */
    private SessionResponseBody authenticateWithBankId()
            throws BankIdException, LoginException, AuthorizationException {
        String requestId = initBankIDAuthenticate();

        collectBankID(String.format(AUTHENTICATE_COLLECT_URL, requestId));

        // Fetch the data for the authentication session.
        return getSessionResponse();
    }

    @Override
    public void clearLoginSession() {
        // Clean the session in memory
        this.sessionId = null;
        this.userInstallationId = null;
        this.sessionResponse = null;

        // Clean the persisted session
        credentials.removePersistentSession();
    }

    /** Helper method to create a client request. */
    private Builder createClientRequest(String url) {
        Builder request =
                client.resource(url)
                        .header("User-Agent", DEFAULT_USER_AGENT)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("ApiKey", API_KEY)
                        .header("ApiVersion", API_VERSION)
                        .header("ClientAppVersion", CLIENT_APP_VERSION)
                        .header("ClientHardware", CLIENT_HARDWARE)
                        .header("ClientOSVersion", CLIENT_OS_VERSION)
                        .header("ClientOS", CLIENT_OS);

        request = request.header("UserInstallationId", userInstallationId);

        if (sessionId != null) {
            request = request.header("SessionId", sessionId);
        }

        return request;
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception {
        if (transfer.getType().equals(TransferType.BANK_TRANSFER)
                || transfer.getType().equals(TransferType.PAYMENT)) {
            executeTransfer(transfer);
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.")
                    .build();
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception {

        switch (transfer.getType()) {
            case EINVOICE:
                approveEInvoice(transfer);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Unhandled transfer type: %s", transfer.getType()));
        }
    }

    private void approveEInvoice(final Transfer transfer)
            throws ExecutionException, RetryException, BankIdException, LoginException,
                    AuthorizationException {
        validateNoUnsignedTransfers();

        String invoiceId = getInvoiceIdFrom(transfer);
        Transfer originalTransfer = getOriginalTransfer(transfer);

        // In the ICA-banken app a user can change amount and due date of the e-invoice. If the
        // change occurs after
        // a refresh we might have the wrong data for those fields when displaying the e-invoice to
        // the user.
        //
        // We check if the e-invoice have been modified, if so the user must refresh their
        // credentials so that we
        // display the latest version of the e-invoice. This is so that the user doesn't approve
        // something else than
        // what is shown in the Tink app.

        final AccountEntity sourceAccount = fetchSourceAccountFor(transfer);
        final EInvoiceEntity eInvoice = findMatchingEInvoice(invoiceId, originalTransfer);
        final Transfer bankTransfer = eInvoice.toTinkTransfer(catalog);

        // Important validateEInvoiceOnOurSide() is called _before_ validateEInvoiceOnTheirSide(...)
        // since the latter
        // might auto-correct due date (or other things). If in wrong order, due date validation on
        // our side will fail.
        validateEInvoiceOnOurSide(transfer, bankTransfer);
        updateIfNecessary(transfer, bankTransfer, invoiceId);
        validateEInvoiceOnTheirSide(sourceAccount, invoiceId);

        if (!eInvoice.isRecipientConfirmed()) {
            signInvoice(invoiceId);
        }

        acceptEInvoiceTransfer(sourceAccount.getAccountId(), invoiceId);
    }

    private void validateEInvoiceOnOurSide(
            final Transfer potentiallyModifiedTransfer, final Transfer bankTransfer) {

        // Destination account.
        validateFieldsMatch(
                bankTransfer.getDestination(),
                potentiallyModifiedTransfer.getDestination(),
                EndUserMessage.EINVOICE_MODIFY_DESTINATION,
                "Destination account cannot be changed.");

        // Type
        Preconditions.checkState(
                Objects.equal(bankTransfer.getType(), potentiallyModifiedTransfer.getType()));

        // Source message
        validateFieldsMatch(
                bankTransfer.getSourceMessage(),
                potentiallyModifiedTransfer.getSourceMessage(),
                EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE,
                "Source message cannot be changed.");

        // Destination message
        validateFieldsMatch(
                bankTransfer.getDestinationMessage(),
                potentiallyModifiedTransfer.getDestinationMessage(),
                EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE,
                "Destination message cannot be changed.");
    }

    private void validateFieldsMatch(
            Object oldField,
            Object potentiallyModifiedField,
            EndUserMessage endUserMessage,
            String internalMessage) {
        if (!Objects.equal(oldField, potentiallyModifiedField)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString(endUserMessage))
                    .setMessage(
                            String.format(
                                    "%s Old: %s Current: %s",
                                    internalMessage,
                                    potentiallyModifiedField,
                                    potentiallyModifiedField))
                    .build();
        }
    }

    private void updateIfNecessary(
            final Transfer potentiallyModifiedTransfer,
            final Transfer bankTransfer,
            String invoiceId) {
        boolean shouldUpdate = false;
        Amount amountForUpdate = bankTransfer.getAmount();
        Date dueDateForUpdate = bankTransfer.getDueDate();

        // Amount
        if (!Objects.equal(bankTransfer.getAmount(), potentiallyModifiedTransfer.getAmount())) {
            amountForUpdate = potentiallyModifiedTransfer.getAmount();
            shouldUpdate = true;
        }

        // Due date
        if (!Objects.equal(bankTransfer.getDueDate(), potentiallyModifiedTransfer.getDueDate())) {
            dueDateForUpdate = potentiallyModifiedTransfer.getDueDate();
            shouldUpdate = true;
        }

        if (shouldUpdate) {
            updateEInvoice(dueDateForUpdate, amountForUpdate, invoiceId);
        }
    }

    private void updateEInvoice(Date dueDate, Amount amount, String invoiceId) {
        UpdateEInvoiceRequest updateEInvoiceRequest = new UpdateEInvoiceRequest();

        updateEInvoiceRequest.setPayDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate));
        updateEInvoiceRequest.setFormattedAmount(amount.getValue());
        updateEInvoiceRequest.setInvoiceId(invoiceId);

        ValidateEInvoiceResponse response =
                createClientRequest(UPDATE_INVOICE_URL)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(ValidateEInvoiceResponse.class, updateEInvoiceRequest);

        if (response.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Could not update invoice: %s", response))
                    .setEndUserMessage(
                            getEndUserMessage(response, EndUserMessage.PAYMENT_UPDATE_FAILED))
                    .build();
        }
    }

    private void validateEInvoiceOnTheirSide(final AccountEntity account, final String invoiceId)
            throws ExecutionException, RetryException {

        ValidateEInvoiceResponse validateResponse =
                WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER.call(
                        () -> validateEInvoice(account.getAccountId(), invoiceId));

        if (validateResponse.isValidationError()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Could not validate invoice: %s", validateResponse))
                    .setEndUserMessage(
                            getEndUserMessage(
                                    validateResponse, EndUserMessage.EINVOICE_VALIDATE_FAILED))
                    .build();
        }
    }

    private void signInvoice(final String invoiceId)
            throws BankIdException, LoginException, AuthorizationException {
        String requestId = initBankIDSignEInvoice(invoiceId);

        collectBankID(String.format(SIGN_TRANSFER_COLLECT_URL, requestId));

        endBankIDAuthentication(requestId, invoiceId);
    }

    private ValidateEInvoiceResponse validateEInvoice(
            final String accountId, final String eInvoiceId) {
        // Might return 409 - Angivet datum har ändrats till närmast möjliga dag. See
        // WHILE_ICABANKEN_CORRECTS_VALIDATION_ERROR_RETRYER.

        ValidateEInvoiceRequest validateRequest = new ValidateEInvoiceRequest();
        validateRequest.setAccountId(accountId);
        validateRequest.setInvoiceId(eInvoiceId);

        ClientResponse response =
                createClientRequest(VALIDATE_INVOICE_URL)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(ClientResponse.class, validateRequest);
        try {
            return response.getEntity(ValidateEInvoiceResponse.class);
        } finally {
            response.close();
        }
    }

    /** End the bankid authentication. */
    private void endBankIDAuthentication(String requestId, String invoiceId) {
        EndBankIdAuthenticationRequest request = new EndBankIdAuthenticationRequest();
        request.setRequestId(requestId);
        request.setInvoiceId(invoiceId);

        createClientRequest(END_BANKID_AUTHENTICATION_URL)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, request)
                .close();
    }

    private void acceptEInvoiceTransfer(String accountId, String invoiceId) {
        AcceptEInvoiceTransferRequest request = new AcceptEInvoiceTransferRequest();
        request.setDebitAccountId(accountId);
        request.setInvoiceId(invoiceId);

        createClientRequest(ACCEPT_EINVOICE_URL)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, request)
                .close();
    }

    private void executeTransfer(final Transfer transfer) throws Exception {
        validateNoUnsignedTransfers();

        List<AccountEntity> accounts = fetchAccounts();

        AccountEntity sourceAccount = findSourceAccount(transfer.getSource(), accounts);
        RecipientEntity destinationAccount = findDestinationAccount(transfer, accounts);

        try {
            makeTransfer(transfer, sourceAccount, destinationAccount);
            validateAndSignTransfer();
        } catch (Exception initialException) {
            try {
                AssignmentsResponse unsignedTransferResponse = fetchUnsignedTransfers();
                deleteUnsignedTransfer(unsignedTransferResponse.getBody());
            } catch (Exception deleteException) {
                log.warn(
                        "Could not delete transfer in outbox. "
                                + "If unsigned transfers are left here, user could end up in a deadlock.",
                        deleteException);
            }

            if (!isTransferFailedButWasSuccessful(transfer, sourceAccount)) {
                throw initialException;
            }
        }
    }

    private boolean isMatchingTransfers(
            Transfer newTransfer, UpcomingTransactionEntity upcomingTransaction) {
        return !(newTransfer == null || upcomingTransaction == null)
                && Objects.equal(newTransfer.getHash(), upcomingTransaction.getHash(false));
    }

    private boolean isTransferFailedButWasSuccessful(
            Transfer transfer, AccountEntity sourceAccount) {
        UpcomingTransactionsBody upcomingTransactions = fetchUpcomingTransactions();
        List<UpcomingTransactionEntity> transactionsFor =
                upcomingTransactions.findUpcomingTransactionsFor(sourceAccount);
        for (UpcomingTransactionEntity upcomingTransaction : transactionsFor) {
            if (isMatchingTransfers(transfer, upcomingTransaction)) {
                return true;
            }
        }

        return false;
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(
            List<Account> updatedAccounts,
            List<AccountEntity> accountEntities,
            List<RecipientEntity> recipientEntities)
            throws Exception {
        List<GeneralAccountEntity> sourceAccounts =
                findSourceAccountsFor(IcaSourceType.TRANSFER, accountEntities);
        List<GeneralAccountEntity> destinationAccounts =
                getAllDestinationAccountsFor(
                        IcaDestinationType.TRANSFER, accountEntities, recipientEntities);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            List<Account> accounts,
            List<AccountEntity> accountEntities,
            List<RecipientEntity> recipientEntities)
            throws Exception {
        List<GeneralAccountEntity> sourceAccounts =
                findSourceAccountsFor(IcaSourceType.PAYMENT, accountEntities);
        List<GeneralAccountEntity> destinationAccounts =
                findDestinationAccountsFor(IcaDestinationType.PAYMENT, recipientEntities);

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(accounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    private List<GeneralAccountEntity> findSourceAccountsFor(
            IcaSourceType sourceType, List<AccountEntity> accountEntities) {
        return accountEntities
                .stream()
                .filter(accountEntity -> sourceType.contains(accountEntity.getValidFor()))
                .collect(Collectors.toList());
    }

    private List<GeneralAccountEntity> getAllDestinationAccountsFor(
            IcaDestinationType destinationType,
            List<AccountEntity> accountEntities,
            List<RecipientEntity> recipientEntities)
            throws Exception {
        List<GeneralAccountEntity> destinationAccounts =
                findDestinationAccountsFor(destinationType, recipientEntities);

        accountEntities
                .stream()
                .filter(accountEntity -> destinationType.contains(accountEntity.getValidFor()))
                .forEach(destinationAccounts::add);

        return destinationAccounts;
    }

    private List<GeneralAccountEntity> findDestinationAccountsFor(
            IcaDestinationType destinationType, List<RecipientEntity> recipientEntities) {
        return recipientEntities
                .stream()
                .filter(recipientEntity -> destinationType.contains(recipientEntity.getType()))
                .collect(Collectors.toList());
    }

    private EInvoiceResponse fetchEInvoices() {
        return createClientRequest(EINVOICES_URL)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(EInvoiceResponse.class);
    }

    private FundDetails fetchFundDetails(String fundId) {
        FundsResponse fundsResponse =
                createClientRequest(String.format(FUNDS_URL, fundId)).get(FundsResponse.class);

        return fundsResponse.getBody();
    }

    private RecipientEntity findDestinationAccount(
            final Transfer transfer, List<AccountEntity> accounts) {
        if (transfer.getType().equals(TransferType.BANK_TRANSFER)) {
            Optional<AccountEntity> ownAccount =
                    tryFindOwnAccount(transfer.getDestination(), accounts);

            if (ownAccount.isPresent()) {
                return new OwnRecipientEntity(ownAccount.get());
            }
        }

        return tryFindRegisteredDestinationAccount(transfer.getDestination())
                .orElseGet(
                        () ->
                                addDestination(transfer.getDestination())
                                        .orElseThrow(
                                                () ->
                                                        TransferExecutionException.builder(
                                                                        SignableOperationStatuses
                                                                                .FAILED)
                                                                .setEndUserMessage(
                                                                        context.getCatalog()
                                                                                        .getString(
                                                                                                EndUserMessage
                                                                                                        .INVALID_DESTINATION)
                                                                                + ": "
                                                                                + transfer.getDestination()
                                                                                        .getIdentifier(
                                                                                                ACCOUNT_IDENTIFIER_FORMATTER))
                                                                .build()));
    }

    private AccountEntity fetchSourceAccountFor(Transfer transfer) {
        List<AccountEntity> accounts = fetchAccounts();
        return findSourceAccount(transfer.getSource(), accounts);
    }

    private AccountEntity findSourceAccount(
            final AccountIdentifier source, List<AccountEntity> accounts) {
        Optional<AccountEntity> fromAccount =
                accounts.stream()
                        .filter(
                                ae ->
                                        (Objects.equal(
                                                source.getIdentifier(DEFAULT_FORMATTER),
                                                ae.getAccountNumber()
                                                        .replace(" ", "")
                                                        .replace("-", ""))))
                        .findFirst();

        return fromAccount.orElseThrow(
                () ->
                        TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setEndUserMessage(
                                        context.getCatalog()
                                                .getString(EndUserMessage.INVALID_SOURCE))
                                .build());
    }

    /**
     * For first page, don't set any toDate. For second page, set toDate = fromDate - 1, fall back
     * to one month (like IcaBanken App). For all other pages, use last transaction in list date -
     * 1, fall back to today (like IcaBanken App).
     */
    private String findToDateParameter(
            ArrayList<Transaction> transactions,
            TransactionListResponse transactionsResponse,
            int page)
            throws ParseException {

        if (page == 2) {
            if (transactionsResponse == null) {
                return DATE_FORMAT.format(
                        org.apache.commons.lang3.time.DateUtils.addMonths(
                                DateUtils.getToday(), -1));
            } else {
                return DATE_FORMAT.format(
                        DateUtils.addDays(
                                DATE_FORMAT.parse(transactionsResponse.getBody().getFromDate()),
                                -1));
            }
        }

        if (page > 2) {
            return DATE_FORMAT.format(
                    java.util.Optional.<List<Transaction>>ofNullable(transactions)
                            .orElse(ImmutableList.of())
                            .stream()
                            .min(
                                    TransactionOrdering
                                            .TRANSACTION_DATE_ORDERING) // Oldest transaction.
                            .map(Transaction::getDate)
                            .map(t -> DateUtils.addDays(t, -1))
                            .orElse(DateUtils.getToday()));
        }
        return "";
    }

    private SessionResponseBody getSessionResponse() {
        if (sessionId == null) {
            return null;
        }

        String deviceApplicationId = credentials.getSensitivePayload(DEVICEAPPLICATIONID_KEY);
        if (Strings.isNullOrEmpty(deviceApplicationId)) {
            return null;
        }

        return createClientRequest(String.format(SESSION_URL, deviceApplicationId))
                .get(SessionResponse.class)
                .getBody();
    }

    /** Verifies that we are logged in by requesting a new session */
    @Override
    public boolean isLoggedIn() throws Exception {
        if (sessionId == null || userInstallationId == null) {
            return false;
        }

        String deviceApplicationId = credentials.getSensitivePayload(DEVICEAPPLICATIONID_KEY);
        if (Strings.isNullOrEmpty(deviceApplicationId)) {
            return false;
        }

        ClientResponse clientResponse =
                createClientRequest(String.format(SESSION_URL, deviceApplicationId))
                        .get(ClientResponse.class);

        try {
            boolean loggedIn = clientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK;

            if (loggedIn) {
                // We are still logged in so update the login session
                this.sessionResponse = clientResponse.getEntity(SessionResponseBody.class);
            }

            return loggedIn;
        } finally {
            clientResponse.close();
        }
    }

    /**
     * Keeping the agent alive by doing a get request to their heartbeat endpoint. Their app is
     * doing a request every 5th minute to keep the agent alive.
     */
    @Override
    public boolean keepAlive() throws Exception {
        if (sessionId == null || userInstallationId == null) {
            return false;
        }

        ClientResponse clientResponse =
                createClientRequest(HEARTBEAT_URL).get(ClientResponse.class);

        try {
            return clientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK;
        } finally {
            clientResponse.close();
        }
    }

    @Override
    public void loadLoginSession() {
        PersistentSession persistentSession =
                credentials.getPersistentSession(PersistentSession.class);

        if (persistentSession != null) {
            sessionId = persistentSession.getSessionId();
            userInstallationId = persistentSession.getUserInstallationId();
            addSessionCookiesToClient(client, persistentSession);
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        persistNewDeviceApplicationIdIfMissing();

        try {
            switch (credentials.getType()) {
                case MOBILE_BANKID:
                    sessionResponse = authenticateWithBankId();
                    break;
                default:
                    throw new IllegalStateException(
                            "unsupported credentials type: " + credentials.getType());
            }

            Preconditions.checkNotNull(sessionResponse);

            if (sessionResponse.mustUpdateInformationToICABanken()) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        UserMessage.KNOW_YOUR_CUSTOMER.getKey());
            }

            userInstallationId = getUserInstallationId(sessionResponse);
            return true;
        } catch (UniformInterfaceException e) {
            logHttpErrors(e.getResponse());

            throw e;
        }
    }

    /**
     * The application id seems to be random generated from the app installation UUID, so if this is
     * first time for the credential we won't have any deviceApplicationId stored from before. Then
     * generate one for this credential.
     *
     * <p>This ID is later used as query param when upon fetching the SessionResponse.
     */
    private void persistNewDeviceApplicationIdIfMissing() {
        String deviceApplicationId = credentials.getSensitivePayload(DEVICEAPPLICATIONID_KEY);

        if (Strings.isNullOrEmpty(deviceApplicationId)) {
            deviceApplicationId = StringUtils.generateUUID();
            credentials.setSensitivePayload(DEVICEAPPLICATIONID_KEY, deviceApplicationId);
        }
    }

    /**
     * The user installation ID is some sort of unique identifier for the customer that comes from
     * the session response ICA uses this on each logged in request set as a header value.
     */
    private String getUserInstallationId(SessionResponseBody sessionResponse) {
        if (sessionResponse == null || sessionResponse.getCustomer() == null) {
            return null;
        }

        return sessionResponse.getCustomer().getUserInstallationId();
    }

    @Override
    public void logout() throws Exception {
        createClientRequest(LOGOUT_URL).get(String.class);
    }

    private void makeTransfer(
            Transfer transfer, AccountEntity fromAccount, RecipientEntity recipientAccount) {
        TransferRequest transferRequest;
        if (transfer.getType().equals(TransferType.PAYMENT)) {
            PaymentRequest paymentRequest = new PaymentRequest();

            paymentRequest.setReferenceType(ICABankenUtils.getReferenceTypeFor(transfer));
            transferRequest = paymentRequest;
            transferRequest.setMemo(transfer.getSourceMessage());
            transferRequest.setReference(transfer.getDestinationMessage());
        } else {
            transferRequest = new BankTransferRequest();

            TransferMessageFormatter.Messages formattedMessages =
                    transferMessageFormatter.getMessages(transfer, recipientAccount.isOwnAccount());

            transferRequest.setMemo(formattedMessages.getSourceMessage());
            transferRequest.setReference(formattedMessages.getDestinationMessage());
        }

        transferRequest.setAmount(transfer.getAmount().getValue());
        transferRequest.setDueDate(ICABankenUtils.findOrCreateDueDateFor(transfer));
        transferRequest.setFromAccountId(fromAccount.getAccountId());
        transferRequest.setRecipientId(recipientAccount.getRecipientId());
        transferRequest.setRecipientAccountNumber(recipientAccount.getAccountNumber());
        transferRequest.setRecipientType(recipientAccount.getType());
        transferRequest.setType(transfer.getType());

        ClientResponse transferClientResponse =
                createClientRequest(UNSIGNED_ASSIGNMENTS_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .post(ClientResponse.class, transferRequest);

        try {
            int status = transferClientResponse.getStatus();

            // Conflict (409) means the date was a non bank day, update transfer with suggested
            // date.

            if (status == 409) {
                TransferResponse transferResponse =
                        transferClientResponse.getEntity(TransferResponse.class);
                if (transferResponse.getBody().getProposedNewDate() != null) {
                    transferResponse = redoTransferWithValidDate(transferRequest, transferResponse);
                }

                if (transferResponse.getResponseStatus().getCode() != 0) {
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(
                                    getEndUserMessage(
                                            transferResponse,
                                            EndUserMessage.TRANSFER_EXECUTE_FAILED))
                            .build();
                }
            } else if (status != 200) {
                String errorMessage =
                        StringEscapeUtils.unescapeHtml4(
                                transferClientResponse.getHeaders().getFirst("ErrorMessage"));
                if (Strings.isNullOrEmpty(errorMessage)) {
                    errorMessage =
                            context.getCatalog().getString(EndUserMessage.TRANSFER_EXECUTE_FAILED);
                }
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(errorMessage)
                        .build();
            }
        } finally {
            transferClientResponse.close();
        }
    }

    @Override
    public void persistLoginSession() {
        PersistentSession persistentSession = new PersistentSession();
        persistentSession.setSessionId(sessionId);
        persistentSession.setUserInstallationId(userInstallationId);
        persistentSession.setCookiesFromClient(client);

        credentials.setPersistentSession(persistentSession);
    }

    /** Process an account and fetch all the transactions for that account. */
    private Pair<Account, List<Transaction>> processAccount(
            AccountEntity accountEntity, UpcomingTransactionsBody upcomingTransactions)
            throws Exception {
        Account account = accountEntity.toAccount();

        ArrayList<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(upcomingTransactions.findTransactionsFor(accountEntity));

        if (accountEntity.getOutstandingAmount() != 0) {
            String reservedTransactionsUrl =
                    String.format(RESERVED_TRANSACTIONS_URL, accountEntity.getAccountId());

            TransactionListResponse reservedTransactionsResponse =
                    createClientRequest(reservedTransactionsUrl).get(TransactionListResponse.class);

            for (TransactionEntity transactionEntity :
                    reservedTransactionsResponse.getBody().getTransactions()) {
                transactions.add(transactionEntity.toTransaction(true));
            }
        }

        TransactionListResponse transactionsResponse = null;

        int page = 0;
        String oldToDate = null;

        do {
            page++;

            String toDate = findToDateParameter(transactions, transactionsResponse, page);

            // Make sure that findToDateParameter() doesn't enter a loop, returning the same dates
            // over and over again.
            if (oldToDate != null && oldToDate.equals(toDate)) {
                break;
            }
            oldToDate = toDate;

            String transactionsUrl =
                    String.format(TRANSACTIONS_URL, accountEntity.getAccountId(), toDate);

            transactionsResponse =
                    createClientRequest(transactionsUrl).get(TransactionListResponse.class);

            for (TransactionEntity transactionEntity :
                    transactionsResponse.getBody().getTransactions()) {
                transactions.add(transactionEntity.toTransaction());
            }
        } while (!transactionsResponse.getBody().isNoMoreTransactions()
                && !isContentWithRefresh(account, transactions));

        return Pair.of(account, transactions);
    }

    private TransferResponse redoTransferWithValidDate(
            TransferRequest transferRequest, TransferResponse transferResponse) {
        transferRequest.setDueDate(transferResponse.getBody().getProposedNewDate());

        return createClientRequest(UNSIGNED_ASSIGNMENTS_URL)
                .type(MediaType.APPLICATION_JSON)
                .post(TransferResponse.class, transferRequest);
    }

    private void updateTransferDestinations() {
        List<AccountEntity> accountEntities = getAccounts();
        List<RecipientEntity> recipientEntities = fetchDestinationAccounts();

        TransferDestinationsResponse response = new TransferDestinationsResponse();

        try {
            response.addDestinations(
                    getTransferAccountDestinations(
                            systemUpdater.getUpdatedAccounts(),
                            accountEntities,
                            recipientEntities));
            response.addDestinations(
                    getPaymentAccountDestinations(
                            systemUpdater.getUpdatedAccounts(),
                            accountEntities,
                            recipientEntities));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        systemUpdater.updateTransferDestinationPatterns(response.getDestinations());
    }

    private List<AccountEntity> getAccounts() {
        if (accountEntities != null) {
            return accountEntities;
        }

        accountEntities = fetchAccounts();
        return accountEntities;
    }

    private void logDepots(List<DepotEntity> depots) {
        LogTag investmentLogTag = LogTag.from("icabanken_investment");
        try {
            String logMessage = SerializationUtils.serializeToString(depots);
            log.infoExtraLong(logMessage, investmentLogTag);
        } catch (Exception e) {
            log.info("error when fetching investment " + investmentLogTag.toString());
        }
    }

    private Optional<Instrument> toInstrument(FundHoldingsEntity fund, FundDetails fundDetails) {
        Instrument instrument = new Instrument();

        if (fund.getShares() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(fund.getInvestedAmount() / fund.getShares());
        instrument.setCurrency(fundDetails.getTradingCode());
        instrument.setIsin(fundDetails.getIsin());
        instrument.setMarketPlace(fundDetails.getTradingCode());
        instrument.setMarketValue(fund.getMarketValue());
        instrument.setName(fund.getFundName());
        instrument.setPrice(fundDetails.getNewAssetValue());
        instrument.setProfit(fund.getMarketValue() - fund.getInvestedAmount());
        instrument.setQuantity(fund.getShares());
        instrument.setRawType(fundDetails.getCategory());
        instrument.setType(Instrument.Type.FUND); // Currently only possible to buy funds at ICA
        instrument.setUniqueIdentifier(fundDetails.getIsin() + fundDetails.getTradingCode());

        return Optional.of(instrument);
    }

    private void logLoansIfNotEmpty(List<LoanEntity> loansList) {
        if (!loansList.isEmpty()) {
            log.infoExtraLong(
                    SerializationUtils.serializeToString(loansList),
                    LogTag.from("icabanken-blanco-loans"));
        }
    }

    private Pair<Account, AccountFeatures> updateAccount(LoanEntity loanEntity) throws Exception {
        Account account = loanEntity.toAccount();
        Loan loan = loanEntity.toLoan();

        return Pair.of(account, AccountFeatures.createForLoan(loan));
    }

    private Pair<Account, AccountFeatures> updateAccount(MortgageEntity mortgageEntity)
            throws Exception {
        Account account = mortgageEntity.toAccount();
        Loan loan = mortgageEntity.toLoan();

        return Pair.of(account, AccountFeatures.createForLoan(loan));
    }

    private Optional<RecipientEntity> tryFindRegisteredDestinationAccount(
            AccountIdentifier destination) {
        if (Objects.equal(destination.getType(), AccountIdentifier.Type.SE)) {
            return tryFindRegisteredTransferAccount(destination);
        }

        return tryFindRegisteredPaymentAccount(destination);
    }

    private Optional<AccountEntity> tryFindOwnAccount(
            final AccountIdentifier destination, List<AccountEntity> accounts) {
        return accounts.stream()
                .filter(
                        ae ->
                                (Objects.equal(
                                        destination.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER),
                                        ae.getAccountNumber().replace(" ", "").replace("-", ""))))
                .findFirst();
    }

    private Optional<RecipientEntity> tryFindRegisteredPaymentAccount(
            final AccountIdentifier destination) {
        List<RecipientEntity> destinationAccounts = fetchDestinationAccounts();

        return destinationAccounts
                .stream()
                .filter(
                        re ->
                                (Objects.equal(
                                        destination.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER),
                                        re.getAccountNumber())))
                .findFirst();
    }

    private Optional<RecipientEntity> tryFindRegisteredTransferAccount(
            final AccountIdentifier destination) {
        List<RecipientEntity> destinationAccounts = fetchDestinationAccounts();

        return destinationAccounts
                .stream()
                .filter(
                        re ->
                                (Objects.equal(
                                        destination.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER),
                                        re.getAccountNumber().replace(" ", "").replace("-", ""))))
                .findFirst();
    }

    private void validateAndSignTransfer()
            throws BankIdException, LoginException, AuthorizationException {
        // If internal transfer, no need to sign.
        if (!hasUnsignedTransfers()) {
            return;
        }

        String requestId = initBankIDSignTransfer();

        collectBankID(String.format(SIGN_TRANSFER_COLLECT_URL, requestId));
        SignedAssignmentList assignments = getSignedAssignmentsList(requestId);

        if (assignments.containRejected()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Transfer rejected by ICA")
                    .setEndUserMessage(catalog.getString(EndUserMessage.TRANSFER_EXECUTE_FAILED))
                    .build();
        }
    }

    private void deleteUnsignedTransfer(AssignmentsResponseBody responseBody) {
        // Should not be more than one transfer to cancel.
        if (responseBody.getAssignments() != null && responseBody.getAssignments().size() == 1) {
            String transferId = responseBody.getAssignments().get(0).getRegistrationId();
            createClientRequest(String.format(DELETE_UNSIGNED_TRANSFER_URL, transferId))
                    .type(MediaType.APPLICATION_JSON)
                    .delete();
        }
    }

    private void validateNoUnsignedTransfers() {
        if (hasUnsignedTransfers()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            context.getCatalog()
                                    .getString(EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                    .build();
        }
    }

    private List<AccountEntity> fetchAccounts() {
        AccountsResponse accountsResponse =
                createClientRequest(ACCOUNTS_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .get(AccountsResponse.class);

        return accountsResponse.getBody().getAccounts().concatenateAccounts();
    }

    private List<DepotEntity> fetchDepots() {
        DepotsResponse depotsResponse =
                createClientRequest(DEPOTS_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .get(DepotsResponse.class);

        return depotsResponse.getBody().getDepots();
    }

    private List<RecipientEntity> fetchDestinationAccounts() {
        RecipientsResponse recipientResponse =
                createClientRequest(TRANSFER_DESTINATIONS_URL)
                        .type(MediaType.APPLICATION_JSON)
                        .get(RecipientsResponse.class);

        return recipientResponse.getBody().getRecipients();
    }

    private AssignmentsResponse fetchUnsignedTransfers() {
        return createClientRequest(UNSIGNED_TRANSFERS_URL)
                .type(MediaType.APPLICATION_JSON)
                .get(AssignmentsResponse.class);
    }

    private UpcomingTransactionsBody fetchUpcomingTransactions() {
        UpcomingTransactionsResponse upcomingTransactionsResponse =
                createClientRequest(UPCOMING_TRANSACTIONS_URL)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .get(UpcomingTransactionsResponse.class);

        return upcomingTransactionsResponse.getBody();
    }

    private String initBankIDAuthenticate() throws BankIdException {
        String url = String.format(INIT_BANKID_LOGIN_URL, credentials.getField(Field.Key.USERNAME));
        return initBankID(url, true);
    }

    private String initBankIDSignEInvoice(String invoiceId) throws BankIdException {
        String url = String.format(INIT_EINVOICE_SIGN_URL, invoiceId);
        return initBankID(url, true);
    }

    private String initBankIDSignTransfer() throws BankIdException {
        String url = INIT_TRANSFER_SIGN_URL;
        return initBankID(url, true);
    }

    private String initBankID(String url, boolean useAutostartToken) throws BankIdException {
        try {
            InitBankIdResponse bankIdResponse =
                    createClientRequest(url).post(InitBankIdResponse.class);

            String requestId = extractRequestIdFrom(bankIdResponse);
            String autostartToken = null;
            if (useAutostartToken) {
                autostartToken = bankIdResponse.getBody().getAutostartToken();
            }

            supplementalRequester.openBankId(autostartToken, false);

            return requestId;
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();

            if (Objects.equal(response.getStatus(), 409)) {
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            }

            throw e;
        }
    }

    private String extractRequestIdFrom(InitBankIdResponse bankIdResponse) {
        String requestId = bankIdResponse.getBody().getRequestId();

        Preconditions.checkState(
                !Strings.isNullOrEmpty(requestId),
                "Couldn't initialize BankID, requestId was null or empty");

        return requestId;
    }

    private String getInvoiceIdFrom(Transfer transfer) {
        final Optional<String> invoiceId =
                transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);

        if (!invoiceId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Missing PROVIDER_UNIQUE_ID on transfer payload")
                    .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceId.get();
    }

    private EInvoiceEntity findMatchingEInvoice(String invoiceId, Transfer originalTransfer) {
        final EInvoiceEntity eInvoice = findEInvoice(invoiceId);
        Transfer transferAtBank = eInvoice.toTinkTransfer(catalog);

        if (!Objects.equal(originalTransfer.getHash(), transferAtBank.getHash())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(UserMessage.EINVOICE_MODIFIED_IN_BANK_APP.getKey().get())
                    .build();
        }

        return eInvoice;
    }

    private EInvoiceEntity findEInvoice(String invoiceId) {
        EInvoiceResponse response = fetchEInvoices();
        Optional<EInvoiceEntity> invoiceEntity = response.getBody().getInvoiceById(invoiceId);

        if (!invoiceEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not find the selected invoice.")
                    .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceEntity.get();
    }

    private SignedAssignmentList getSignedAssignmentsList(String requestId) {
        SignBundleResponse signBundleResponse =
                createClientRequest(String.format(SIGNED_ASSIGNMENTS_URL, requestId))
                        .type(MediaType.APPLICATION_JSON)
                        .post(SignBundleResponse.class, InitSignRequest.bundled());

        return signBundleResponse.getBody().getSignedAssignmentList();
    }

    private boolean hasUnsignedTransfers() {
        AssignmentsResponse unsignedTransferResponse = fetchUnsignedTransfers();

        return !unsignedTransferResponse.getBody().getAssignments().isEmpty();
    }

    private void collectBankID(String collectUrl)
            throws BankIdException, LoginException, AuthorizationException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                CollectBankIdResponseBody collectResponse = fetchBankIDProgress(collectUrl);

                if (collectResponse.isSuccess()) {
                    sessionId =
                            collectResponse.getSessionId() != null
                                    ? collectResponse.getSessionId()
                                    : sessionId;
                    return;
                }

                if (collectResponse.isFailure()) {
                    throw BankIdError.CANCELLED.exception();
                }
            } catch (UniformInterfaceException e) {
                ClientResponse response = e.getResponse();
                CollectBankIdResponse bankIdResponse =
                        response.getEntity(CollectBankIdResponse.class);

                if (response.getStatus() == 409) {

                    if (bankIdResponse.getBody().isTimeOut()) {
                        throw BankIdError.TIMEOUT.exception();
                    }

                    if (bankIdResponse.getBody().isFailure()) {
                        throw BankIdError.CANCELLED.exception();
                    }

                    if (bankIdResponse
                            .getResponseStatus()
                            .getServerMessage()
                            .toLowerCase()
                            .contains("no active accounts")) {
                        throw LoginError.NOT_CUSTOMER.exception();
                    }

                    if (bankIdResponse
                            .getResponseStatus()
                            .getClientMessage()
                            .toLowerCase()
                            .contains("fel personnummer eller lösenord")) {
                        throw LoginError.INCORRECT_CREDENTIALS.exception();
                    }

                    if (bankIdResponse
                            .getResponseStatus()
                            .getClientMessage()
                            .toLowerCase()
                            .contains("konto har ännu inte blivit verifierat")) {
                        throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                    }
                }

                throw e;
            }

            // Wait 2 seconds before checking the status again.
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private CollectBankIdResponseBody fetchBankIDProgress(String url) {
        CollectBankIdResponseBody collectResponse =
                createClientRequest(url).get(CollectBankIdResponse.class).getBody();

        log.info("Awaiting BankID authentication " + collectResponse.getStatus());

        return collectResponse;
    }

    private void logHttpErrors(ClientResponse response) {
        List<String> errors = extractErrorsFrom(response.getHeaders());

        MoreObjects.ToStringHelper errorBuilder =
                MoreObjects.toStringHelper("Response").add("status", response.getStatus());

        int i = 1;

        if (!errors.isEmpty()) {
            for (String error : errors) {
                String key = "error " + i + ":";
                errorBuilder.add(key, error);

                i++;
            }
        }

        log.warn(errorBuilder.toString());
    }

    private List<String> extractErrorsFrom(MultivaluedMap<String, String> headers) {
        if (headers == null) {
            return Lists.newArrayList();
        }

        List<String> errors = headers.get("ErrorMessages");

        return errors != null ? errors : Lists.<String>newArrayList();
    }

    /**
     * Attempts to get a detailed error message from the bank. If not present, it takes the more
     * general alternative.
     */
    private String getEndUserMessage(Response errorResponse, EndUserMessage generalErrorMessage) {
        String message = errorResponse.getResponseStatus().getClientMessage();
        if (Strings.isNullOrEmpty(message)) {
            message = context.getCatalog().getString(generalErrorMessage);
        }
        return message;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return fetchAccountsPerType(RefreshableItem.CHECKING_ACCOUNTS);
    }

    //////////// Refresh Executor Refactor /////////////

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return fetchTransactionsPerAccountType(RefreshableItem.CHECKING_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return fetchAccountsPerType(RefreshableItem.CREDITCARD_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return fetchTransactionsPerAccountType(RefreshableItem.CREDITCARD_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return fetchAccountsPerType(RefreshableItem.SAVING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return fetchTransactionsPerAccountType(RefreshableItem.SAVING_TRANSACTIONS);
    }

    private FetchAccountsResponse fetchAccountsPerType(RefreshableItem type) {
        List<Account> accounts = new ArrayList<>();
        getAccounts()
                .stream()
                .map(AccountEntity::toAccount)
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(accounts::add);
        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return fetchTransactionsPerAccountType(RefreshableItem.LOAN_TRANSACTIONS);
    }

    private FetchTransactionsResponse fetchTransactionsPerAccountType(RefreshableItem type) {
        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();

        UpcomingTransactionsBody upcomingTransactions = fetchUpcomingTransactions();

        for (AccountEntity accountEntity : getAccounts()) {
            Account tinkAccount = accountEntity.toAccount();
            if (!type.isAccountType(tinkAccount.getType())) {
                continue;
            }

            try {
                Pair<Account, List<Transaction>> accountTransaction =
                        processAccount(accountEntity, upcomingTransactions);
                transactionsMap.put(accountTransaction.first, accountTransaction.second);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return new FetchTransactionsResponse(transactionsMap);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        try {
            Map<Account, AccountFeatures> accounts = new HashMap<>();
            List<DepotEntity> depots = fetchDepots();
            logDepots(depots);
            for (DepotEntity depot : depots) {
                Account account = depot.toAccount();
                Portfolio portfolio = depot.toPortfolio();

                List<Instrument> instruments = Lists.newArrayList();

                depot.getFundHoldings()
                        .forEach(
                                fund ->
                                        toInstrument(fund, fetchFundDetails(fund.getFundId()))
                                                .ifPresent(instruments::add));

                portfolio.setInstruments(instruments);

                accounts.put(account, AccountFeatures.createForPortfolios(portfolio));
            }
            return new FetchInvestmentAccountsResponse(accounts);
        } catch (Exception e) {
            // Don't fail the whole refresh just because we failed updating investment data but log
            // error.
            log.error("Caught exception while updating investment data", e);
            return new FetchInvestmentAccountsResponse(Collections.emptyMap());
        }
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        try {
            Map<Account, AccountFeatures> accounts = new HashMap<>();
            EngagementResponseBody engagementResponse =
                    createClientRequest(ENGAGEMENTS_URL).get(EngagementResponse.class).getBody();

            if (engagementResponse.hasLoans()) {

                LoansResponseBody loansResponseBody =
                        createClientRequest(LOANS_URL).get(LoansResponse.class).getBody();

                LoanListEntity loansList = loansResponseBody.getLoanList();
                MortgageListEntity mortgageList = loansResponseBody.getMortgageList();

                if (loansList.getLoans() != null) {

                    logLoansIfNotEmpty(loansList.getLoans());

                    for (LoanEntity loanEntity : loansList.getLoans()) {
                        Pair<Account, AccountFeatures> account = updateAccount(loanEntity);
                        accounts.put(account.first, account.second);
                    }
                }

                if (mortgageList.getMortgages() != null) {
                    for (MortgageEntity mortgageEntity : mortgageList.getMortgages()) {
                        Pair<Account, AccountFeatures> account = updateAccount(mortgageEntity);
                        accounts.put(account.first, account.second);
                    }
                }
            }
            return new FetchLoanAccountsResponse(accounts);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    private enum UserMessage implements LocalizableEnum {
        KNOW_YOUR_CUSTOMER(
                new LocalizableKey(
                        "To be able to refresh your accounts you need to update your customer info in the ICA bank app.")),
        EINVOICE_MODIFIED_IN_BANK_APP(
                new LocalizableKey(
                        "If the e-invoice has been modified in the ICA Banken app, please refresh you credentials."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    ////////////////////////////////////////////////////
}
