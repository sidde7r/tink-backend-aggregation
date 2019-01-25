package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MessageContainer;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdResourceHelper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.BankIdServiceType;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.PaymentAccountWrapper;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AbstractChallengeResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BillRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BillResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.ChallengeResponseRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceApproveRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceApproveResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceDetailsResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceListResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.EInvoiceListTransactionEntity;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitBankIdRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.PortfoliosListResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.SignBankIdResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferAccountEntity;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferDetailsResponse;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.core.SwedishGiroType;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankV2Agent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor {

    private final Client httpClient;

    enum TransactionType {
        FUTURE,
        NORMAL
    }

    private static final DanskeBankAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER = new DanskeBankAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig
            .createWithMaxLength(19, 12);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Don't change! (requires migration)
    private final static String SENSITIVE_PAYLOAD_SECURITY_KEY = "securityKey";
    private static final int OK_STATUS_CODE = 0;
    private static final int INVALID_INVOICE_STATUS_CODE = 9;

    private final DanskeBankApiClient apiClient;
    private final Credentials credentials;
    private final String loginId;
    private final Catalog catalog;
    private final BankIdResourceHelper bankIdResourceHelper;
    private final TransferMessageFormatter transferMessageFormatter;
    private static final int MAX_ATTEMPTS = 90;
    private static final String DEFAULT_ZONE_ID = "CET";

    private Map<AccountEntity, Account> accountMap = null;

    public DanskeBankV2Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        String providerCountry = getProviderCountry();
        String sessionLanguage = "SE";

        credentials = request.getCredentials();
        loginId = getLoginIdFromCredentials(providerCountry, credentials);

        if (Objects.equals(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
            bankIdResourceHelper = new BankIdResourceHelper(credentials);
        } else {
            bankIdResourceHelper = null;
        }

        httpClient = clientFactory.createBasicClient(context.getLogOutputStream());
        this.apiClient = new DanskeBankApiClient(httpClient, DEFAULT_USER_AGENT, bankIdResourceHelper, providerCountry,
                sessionLanguage);
        catalog = context.getCatalog();
        transferMessageFormatter = new TransferMessageFormatter(catalog,
                TRANSFER_MESSAGE_LENGTH_CONFIG,
                new StringNormalizerSwedish(",._-?!/:()&`~"));
    }

    private String getProviderCountry() {
        return request.getProvider().getPayload();
    }

    private static String getLoginIdFromCredentials(String providerCountry, Credentials credentials) {
        if (Objects.equals(providerCountry, "SE")) {
            return credentials.getUsername().substring(2);
        }

        return credentials.getUsername();
    }

    private static Optional<TransferAccountEntity> findAccount(AccountIdentifier identifier,
            List<TransferAccountEntity> transferAccountEntities) {
        final String danskeAdaptedIdentifier = identifier.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

        return transferAccountEntities.stream().filter(
                transferAccountEntity -> Objects.equals(transferAccountEntity.getAccountNumber(), danskeAdaptedIdentifier))
                .findFirst();
    }

    private static HashCode getTransactionHash(HashFunction hashFunction, Transaction transaction) {
        return hashFunction.newHasher()
                .putLong(transaction.getDate().getTime())
                .putString(transaction.getDescription(), Charsets.UTF_8)
                .putDouble(transaction.getAmount())
                .hash();
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(httpClient);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception {
        try {
            switch (transfer.getType()) {
            case BANK_TRANSFER:
                executeBankTransfer(transfer);
                break;
            case PAYMENT:
                executePayment(transfer);
                break;
            case EINVOICE:
                throw new IllegalStateException("Should never happen, einvoices are approved through update");
            default:
                throw new IllegalStateException("Should never happen, not recognized transfer type");
            }
        } catch (BankIdException bankIdException) {
            throw toTransferExecutionException(bankIdException);
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception {
        try {
            switch (transfer.getType()) {
            case EINVOICE:
                approveEInvoice(transfer);
                break;
            case PAYMENT:
                throw new IllegalStateException("Not implemented");
            case BANK_TRANSFER:
                throw new IllegalStateException("Not implemented");
            default:
                throw new IllegalStateException("Should never happen, not recognized transfer type");
            }
        } catch (BankIdException bankIdException) {
            throw toTransferExecutionException(bankIdException);
        }
    }

    private TransferExecutionException toTransferExecutionException(BankIdException bankIdException) {
        BankIdResponse bankIdResponse = bankIdException.getResponse();
        String bankIDStatusCode = bankIdResponse.getBankIdStatusCode();

        if (bankIdResponse.isWaitingForUserInput() || bankIdResponse.isTimeout()) {
            return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
                    .setMessage(
                            bankIDStatusCode + " - Timed out or did not open app: Failed to sign transfer with BankID")
                    .build();
        } else if (bankIdResponse.isUserCancelled()) {
            return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                    .setMessage(bankIDStatusCode + " - User cancelled: Failed to sign transfer with BankID")
                    .build();
        } else {
            String logMessage = bankIDStatusCode + " - Failed to sign transfer with BankID";
            return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            catalog.getString(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED))
                    .setMessage(logMessage).build();
        }
    }

    private void approveEInvoice(final Transfer transfer)
            throws BankIdException, IOException, AuthenticationException {
        Transfer originalTransfer = getOriginalTransfer(transfer);

        ensureValidUpdate(originalTransfer, transfer);

        String transactionId = getTransactionId(originalTransfer);

        EInvoiceDetailsResponse eInvoiceDetails = getEInvoiceDetailsOrThrow(transactionId);
        String fromAccountId = getFromAccountId(transfer, eInvoiceDetails);

        updateAndSignEInvoice(transactionId, fromAccountId, transfer);
    }

    private static Transfer getOriginalTransfer(Transfer transfer) {
        Optional<Transfer> originalTransfer = transfer.getOriginalTransfer();

        if (!originalTransfer.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No original transfer on payload to compare with.").build();
        }

        return originalTransfer.get();
    }

    private String getTransactionId(Transfer originalTransfer) {
        String transactionId = originalTransfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID).orElse(null);

        if (Strings.isNullOrEmpty(transactionId)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Missing PROVIDER_UNIQUE_ID on transfer payload")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return transactionId;
    }

    private EInvoiceDetailsResponse getEInvoiceDetailsOrThrow(String transactionId) {
        EInvoiceDetailsResponse eInvoiceDetails;

        try {
            eInvoiceDetails = apiClient.getEInvoiceDetails(transactionId);
        } catch (Exception fetchException) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .setMessage("Got exception while fetching details of einvoice. Missing?")
                    .setException(fetchException).build();
        }

        return eInvoiceDetails;
    }

    private String getFromAccountId(Transfer transfer, EInvoiceDetailsResponse eInvoiceDetails) {
        final AccountIdentifier source = transfer.getSource();
        List<AccountEntity> fromAccounts = eInvoiceDetails.getFromAccounts();

        Optional<AccountEntity> sourceAccountMatch = fromAccounts.stream()
                .filter(fromAccount -> Objects.equals(
                        EInvoiceDetailsResponse.toAccountIdentifier(fromAccount), source))
                .findFirst();

        if (!sourceAccountMatch.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .setMessage("No source account found")
                    .build();
        }

        return sourceAccountMatch.get().getAccountId();
    }

    /**
     * Only from-account, date and amount is possible to modify on einvoices
     */
    private void ensureValidUpdate(Transfer originalTransfer, Transfer transfer) {
        if (!Objects.equals(transfer.getDestinationMessage(), originalTransfer.getDestinationMessage())) {
            throwFailed(TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_MESSAGE);
        } else if (!Objects.equals(transfer.getDestination(), originalTransfer.getDestination())) {
            throwFailed(TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_DESTINATION);
        }
    }

    private void throwFailed(TransferExecutionException.EndUserMessage endUserMessage) {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString(endUserMessage))
                .build();
    }

    private void updateAndSignEInvoice(String transactionId, String fromAccountId, Transfer transfer)
            throws BankIdException, IOException, AuthenticationException {
        EInvoiceApproveRequest eInvoiceApproveRequest = new EInvoiceApproveRequest(fromAccountId, transfer);
        EInvoiceApproveResponse eInvoiceApproveResponse = apiClient.approveEInvoice(eInvoiceApproveRequest, transactionId);

        if (eInvoiceApproveResponse.getStatus().getStatusCode() != 0) {
            String statusText = eInvoiceApproveResponse.getStatus().getStatusText();
            String errorMessage = statusText != null ? statusText :
                    catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_SIGN_FAILED);
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Failed to sign einvoice. (statuscode: %d, statustext: %s)",
                            eInvoiceApproveResponse.getStatus().getStatusCode(),
                            eInvoiceApproveResponse.getStatus().getStatusText()))
                    .setEndUserMessage(errorMessage)
                    .build();
        }

        if (!eInvoiceApproveResponse.isChallengeNeeded()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No challenge required for DanskeBank e-invoice!")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_SIGN_FAILED))
                    .build();
        }

        apiClient.confirmEInvoice(requestChallenge(eInvoiceApproveResponse), transactionId);
    }

    private void executeBankTransfer(final Transfer transfer)
            throws IOException, BankIdException, AuthenticationException {
        TransferDetailsResponse detailsResponse = apiClient.getTransferAccounts();

        AccountIdentifier source = transfer.getSource();

        Optional<TransferAccountEntity> fromAccount = findAccount(source, detailsResponse.getFromAccounts());
        if (!fromAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        String bankId = DanskeUtils.getBankId(transfer.getDestination());

        // TODO: Adapt this to multiple markets.
        Calendar transferDate = DateUtils.getCalendar();
        if (!Objects.equals(bankId, DanskeUtils.getBankId(ClearingNumber.Bank.DANSKE_BANK))) {
            transferDate.add(Calendar.DAY_OF_YEAR, 1);
            transferDate = DateUtils.getCurrentOrNextBusinessDay(transferDate);
        }

        // Check if the transfer is between two accounts belonging to the same credentials.
        boolean isBetweenUserAccounts = findAccount(transfer.getDestination(), detailsResponse.getFromAccounts())
                .isPresent();

        TransferRequest transferRequest = createTransferRequest(transfer, fromAccount.get(),
                transferDate.toInstant().atZone(ZoneId.of(DEFAULT_ZONE_ID)).toLocalDate(), isBetweenUserAccounts);
        TransferResponse transferResponse = apiClient.createTransfer(transferRequest);

        if (transferResponse.isChallengeNeeded()) {
            apiClient.confirmTransfer(requestChallenge(transferResponse));
        } else if (transferResponse.isConfirmationNeeded()) {
            apiClient.confirmTransfer(transferResponse.getChallengeData());
        }
    }

    private void executePayment(final Transfer transfer)
            throws BankIdException, IOException, AuthenticationException {
        TransferDetailsResponse detailsResponse = apiClient.getPaymentAccounts();

        AccountIdentifier source = transfer.getSource();

        Optional<TransferAccountEntity> fromAccount = findAccount(source, detailsResponse.getFromAccounts());
        if (!fromAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        BillRequest billRequest = createBillRequest(transfer, fromAccount.get());
        BillResponse billResponse = apiClient.createPayment(billRequest);

        if (billResponse.isBillNotValid()) {
            String errorMessage = billResponse.getText() != null ?
                    billResponse.getText() :
                    catalog.getString(TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED);
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(errorMessage).build();
        } else if (!billResponse.isChallengeNeeded()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No challenge required for DanskeBank payment!")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED))
                    .build();
        }

        apiClient.confirmPayment(requestChallenge(billResponse));
    }

    private ChallengeResponseRequest requestChallenge(AbstractChallengeResponse challenge)
            throws BankIdException, LoginException, IOException {
        ChallengeResponseRequest challengeResponseRequest = new ChallengeResponseRequest();
        challengeResponseRequest.setChallengeData(challenge.getChallengeData());

        if (challenge.isBankId()) {
            supplementalRequester.openBankId();

            String orderReference = getOrderReferenceFromChallenge(challenge);
            verifyBankId(BankIdServiceType.VERIFYSIGN, orderReference);

            challengeResponseRequest.setResponse("0");
        } else {
            String challengeResponse = Optional.ofNullable(requestChallengeResponse(challenge.getChallenge()))
                    .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(
                                    catalog.getString(TransferExecutionException.EndUserMessage.CHALLENGE_NO_RESPONSE))
                            .setMessage("Failed to get challenge response from user")
                            .build());

            challengeResponseRequest.setResponse(challengeResponse);
        }

        return challengeResponseRequest;
    }

    private String getOrderReferenceFromChallenge(AbstractChallengeResponse challenge)
            throws IOException {
        MessageContainer challengeContainer = MAPPER
                .readValue(challenge.getChallenge(), MessageContainer.class);
        SignBankIdResponse signBankIdResponse = challengeContainer
                .decrypt(bankIdResourceHelper, SignBankIdResponse.class);

        return signBankIdResponse.getOrderReference();
    }

    private TransferRequest createTransferRequest(Transfer transfer, TransferAccountEntity fromAccount,
            LocalDate transferDate, boolean isBetweenUserAccounts) throws
            TransferMessageException {

        String destinationAccountId = transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

        // Formatted messages to ensure we don't exceed max limits of chars
        TransferMessageFormatter.Messages formattedMessages = transferMessageFormatter
                .getMessages(transfer, isBetweenUserAccounts);

        return TransferRequest.builder()
                .amount(Double.toString(transfer.getAmount().getValue()))
                .currency(request.getProvider().getCurrency()) // Is this valid?
                .date(transferDate)
                .destinationAccountNumber(destinationAccountId)
                .destinationMessage(formattedMessages.getDestinationMessage())
                .sourceAccountNumber(fromAccount.getAccountId())
                .sourceMessage(formattedMessages.getSourceMessage()).build();
    }

    private BillRequest createBillRequest(Transfer transfer, TransferAccountEntity fromAccount) {
        String destinationAccountId = transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

        BillRequest billRequest = new BillRequest();
        billRequest.setSaveForLaterApproval("false");
        billRequest.setSaveReceiver("false");
        billRequest.setAmount(Double.toString(transfer.getAmount().getValue()));
        billRequest.setFromAccountId(fromAccount.getAccountId());
        billRequest.setToAccountId(destinationAccountId);

        if (transfer.getDestination().is(Type.SE_BG)) {
            billRequest.setBankGiro("true");
        } else {
            billRequest.setBankGiro("false");
        }

        if (thinkThisIsOCR(transfer.getDestinationMessage())) {
            billRequest.setReference(transfer.getDestinationMessage());
        } else {
            billRequest.setReceiverText(transfer.getDestinationMessage());
        }

        billRequest.setDate(transfer.getDueDate());

        return billRequest;
    }

    private boolean thinkThisIsOCR(String message) {
        OcrValidationConfiguration validationConfiguration = OcrValidationConfiguration.softOcr();
        GiroMessageValidator validator = GiroMessageValidator.create(validationConfiguration);

        return validator.validate(message).getValidOcr().isPresent();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(List<Account> updatedAccounts) {
        TransferDetailsResponse transferDetailsResponse = apiClient.getTransferAccounts();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(transferDetailsResponse.getFromAccounts())
                .setDestinationAccounts(transferDetailsResponse.getToAccounts())
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        try {
            if (Objects.equals(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
                loginWithMobileBankId();
                return true;
            } else {
                return loginWithPassword();
            }
        } catch (BankIdException bankIdException) {
            BankIdResponse bankIdResponse = bankIdException.getResponse();
            String bankIDStatusCode = bankIdResponse.getBankIdStatusCode();

            if (bankIdResponse.isUserCancelled()) {
                log.info(bankIDStatusCode + " - User cancelled BankID authentication");
                throw BankIdError.CANCELLED.exception();
            } else if (bankIdResponse.isTimeout() || bankIdResponse.isWaitingForUserInput()) {
                log.info(bankIDStatusCode + " - User timeout authenticating with BankID");
                throw BankIdError.TIMEOUT.exception();
            } else {
                throw new IllegalStateException(String.format("#login-refactoring - DanskeBank - Login failed with BankId status: %s", bankIDStatusCode));
            }
        }
    }

    private void loginWithMobileBankId() throws BankIdException, LoginException {
        InitBankIdRequest initBankIdRequest = new InitBankIdRequest(loginId);
        InitBankIdResponse initBankIdResponse = apiClient.bankIdInitAuth(initBankIdRequest);

        supplementalRequester.openBankId();

        String orderReference = initBankIdResponse.getOrderReference();
        verifyBankId(BankIdServiceType.VERIFYAUTH, orderReference);

        apiClient.createAuthenticatedSession(credentials);
    }

    private void verifyBankId(BankIdServiceType bankIdServiceType, String orderReference)
            throws BankIdException, LoginException {
        BankIdResponse bankIdResponse = null;

        // Solves a bug with a SocketTimeoutException below in postBankIdService() due to calling this too quickly.
        Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            bankIdResponse = apiClient.bankIdVerify(bankIdServiceType, orderReference);

            if (bankIdResponse.isUserAuthenticated()) {
                log.info(bankIdServiceType.toString() + " - User authenticated successfully with BankID");
                return;
            } else if (!bankIdResponse.isWaitingForUserInput()) {
                throw new BankIdException(bankIdResponse);
            }

            log.info(bankIdServiceType.toString() + " - Waiting for user to authenticate using BankID");
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw new BankIdException(bankIdResponse);
    }

    private boolean loginWithPassword() throws AuthenticationException, AuthorizationException {
        apiClient.initAndCreateSession(credentials);

        LoginResponse loginResponse = apiClient.loginWithPassword(createLoginRequest());
        String generalLoginErrorMessage = catalog.getString("Could not login, please try again.");

        if (loginResponse.isChallengeNeeded()) {
            if (!request.isManual()) {
                log.warn("Login requires supplemental information, but the request is not manual.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            credentials.setSensitivePayload(SENSITIVE_PAYLOAD_SECURITY_KEY, loginResponse.getSecurityKey());

            String challengeResponse = requestChallengeResponse(loginResponse.getChallenge());

            Preconditions.checkState(!Strings.isNullOrEmpty(challengeResponse),
                    "#login-refactoring - Login failed with null/empty challenge response, statusCode %s, statusText %s.",
                    loginResponse.getStatus().getStatusCode(),
                    loginResponse.getStatus().getStatusText());

            apiClient.loginConfirmChallenge(loginResponse, challengeResponse);
        }

        int statusCode = loginResponse.getStatus().getStatusCode();
        String errorMessage = loginResponse.getStatus().getStatusText() != null ?
                loginResponse.getStatus().getStatusText() : generalLoginErrorMessage;

        switch (statusCode) {
        case 0:
            return true;
        case 1:
            if (errorMessage.toLowerCase().contains("fel personnummer eller servicekod")) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.PERSONAL_NUMBER_SERVICE_CODE.getKey());
            } else if (errorMessage.toLowerCase().contains("teckna avtal om Servicekod")) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.NO_SERVICE_CODE.getKey());
            }
            throw new IllegalStateException(String.format("#login-refactoring - Login failed with statusCode %s, statusText %s", statusCode, errorMessage));
        case 3:
            if (errorMessage.toLowerCase().contains("du har angivit fel servicekod för många gånger")) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.SERVICE_CODE_BLOCKED.getKey());
            }
            throw new IllegalStateException(String.format("#login-refactoring - Login failed with statusCode %s, statusText %s", statusCode, errorMessage));
        case 4:
            throw new IllegalStateException(String.format("#login-refactoring - Login failed with statusCode %s, statusText %s", statusCode, errorMessage));
            // Looks like there are only when there is some problem at Danskes side, see comment in: https://github.com/tink-ab/tink-backend/pull/5398
            // Not implemented yet.
        default:
            throw new IllegalStateException(String.format("#login-refactoring - Login failed with statusCode %s, statusText %s", statusCode, errorMessage));
        }
    }

    private LoginRequest createLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId(loginId);
        loginRequest.setLoginCode(credentials.getPassword());

        String pinnedSecurityKey = credentials.getSensitivePayload(SENSITIVE_PAYLOAD_SECURITY_KEY);
        if (pinnedSecurityKey != null) {
            loginRequest.setKey(pinnedSecurityKey);
        } else {
            loginRequest.setKey("");
        }

        return loginRequest;
    }

    @Override
    public void logout() throws Exception {
        // Nop
    }

    private Map<AccountEntity, Account> getAccountMap() {
        if (accountMap != null) {
            return accountMap;
        }

        accountMap = new HashMap<>();

        List<AccountEntity> accountEntities = apiClient.getAccounts().getAccounts();
        for (AccountEntity accountEntity : accountEntities) {
            boolean isCreditCardAccount = apiClient.isCreditCardAccount(accountEntity);
            Account tinkAccount = accountEntity.toAccount(isCreditCardAccount);
            accountMap.put(accountEntity, tinkAccount);
        }

        return accountMap;
    }

    private void updateAccountsPerType(RefreshableItem type) {
        getAccountMap().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> financialDataCacher.cacheAccount(set.getValue()));
    }

    private void updateTransactionsPerType(RefreshableItem type) {
        getAccountMap().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> updateTransactions(set.getKey()));
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            try {
                getEInvoices();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case TRANSFER_DESTINATIONS:
            TransferDestinationsResponse response = new TransferDestinationsResponse();
            response.addDestinations(getTransferAccountDestinations(systemUpdater.getUpdatedAccounts()));
            response.addDestinations(getPaymentAccountDestinations(systemUpdater.getUpdatedAccounts()));
            systemUpdater.updateTransferDestinationPatterns(response.getDestinations());
            break;

        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
        case CREDITCARD_ACCOUNTS:
            updateAccountsPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
        case LOAN_TRANSACTIONS:
            updateTransactionsPerType(item);
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                collectInvestmentData();
            } catch (Exception e) {
                // Catch and just exit - This is not yet implemented in our model.
                log.warn("Caught exception while logging investment data", e);
            }
            break;

        case LOAN_ACCOUNTS:
            getAccountMap().entrySet().stream()
                    .filter(set -> RefreshableItem.LOAN_ACCOUNTS.isAccountType(set.getValue().getType()))
                    .forEach(set ->
                            financialDataCacher.cacheAccount(set.getValue(), AccountFeatures.createForLoan(set.getKey().toLoan()))
                    );
            break;
        }
    }

    public void getEInvoices() throws Exception {
        List<Transfer> eInvoices = Lists.newArrayList();

        EInvoiceListResponse eInvoiceList = apiClient.getEInvoices();

        for (EInvoiceListTransactionEntity eInvoiceListTransactionEntity : eInvoiceList.getTransactions()) {
            String transferId = eInvoiceListTransactionEntity.getTransactionId();

            EInvoiceDetailsResponse eInvoiceDetails = apiClient.getEInvoiceDetails(transferId);

            // Old eInvoices are removed
            if (eInvoiceDetails.getStatus() != null &&
                    eInvoiceDetails.getStatus().getStatusCode() == INVALID_INVOICE_STATUS_CODE) {
                continue;
            }

            try {
                // There's an identifier on the list response, add that id as payload for identification of invoice
                Transfer eInvoiceTransfer = eInvoiceDetails.toEInvoiceTransfer(transferId);
                eInvoices.add(eInvoiceTransfer);
            } catch (Exception e) {
                log.warn("Validation failed when trying to save e-invoice", e);
            }
        }
        systemUpdater.updateEinvoices(eInvoices);
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(List<Account> updatedAccounts) {
        TransferDetailsResponse detailsResponse = apiClient.getPaymentAccounts();

        if (detailsResponse == null) {
            return Collections.emptyMap();
        }

        List<TransferAccountEntity> fromAccounts = detailsResponse.getFromAccounts();
        if (fromAccounts == null) {
            return Collections.emptyMap();
        }

        List<GeneralAccountEntity> destinationAccounts = Lists.newArrayList();

        List<TransferAccountEntity> toAccountsBankGiro = detailsResponse.getToAccountsBankGiro();
        if (toAccountsBankGiro != null) {
            destinationAccounts.addAll(toAccountsBankGiro.stream()
                    .map(tae -> new PaymentAccountWrapper(tae, SwedishGiroType.BG))
                    .collect(Collectors.toList()));
        }

        List<TransferAccountEntity> toAccountsPlusGiro = detailsResponse.getToAccountsPlusGiro();
        if (toAccountsPlusGiro != null) {
            destinationAccounts.addAll(toAccountsPlusGiro.stream()
                    .map(tae -> new PaymentAccountWrapper(tae, SwedishGiroType.PG))
                    .collect(Collectors.toList()));
        }

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(fromAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .build();
    }

    private void collectInvestmentData() {
        PortfoliosListResponse portfoliosListResponse = apiClient.getPortfolios();

        if (portfoliosListResponse.getStatus().getStatusCode() != OK_STATUS_CODE ||
                portfoliosListResponse.getPortfolios() == null ||
                portfoliosListResponse.getPortfolios().isEmpty()) {
            return;
        }

        portfoliosListResponse.getPortfolios().forEach(portfolioEntity -> {
            Account account = portfolioEntity.toAccount();
            Portfolio portfolio = portfolioEntity.toPortfolio();

            PapersListResponse portfolioPapers = apiClient.getPortfolioPapers(portfolioEntity.getPortfolioId());

            if (portfolioPapers.getStatus().getStatusCode() != OK_STATUS_CODE ||
                    portfolioPapers.getPapers() == null) {
                financialDataCacher.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
            }

            List<Instrument> instruments = Lists.newArrayList();
            portfolioPapers.getPapers().forEach(paperEntity -> paperEntity.toInstrument().ifPresent(instruments::add));
            portfolio.setInstruments(instruments);

            financialDataCacher.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        });
    }



    private void updateTransactions(AccountEntity accountEntity) {
        boolean isCreditCardAccount = apiClient.isCreditCardAccount(accountEntity);
        Account account = accountEntity.toAccount(isCreditCardAccount);

        List<Transaction> transactions = Lists.newArrayList();

        transactions.addAll(fetchTransactions(accountEntity, account, TransactionType.FUTURE));
        transactions.addAll(fetchTransactions(accountEntity, account, TransactionType.NORMAL));

        transactions = filterFauxDoubleCharges(transactions);

        financialDataCacher.updateTransactions(account, transactions);
    }

    private List<Transaction> fetchTransactions(AccountEntity accountEntity, Account account, TransactionType type) {
        List<Transaction> transactions = Lists.newArrayList();

        AccountResponse accountResponse = null;
        do {
            accountResponse = apiClient.getTransactions(accountEntity, type, Optional.ofNullable(accountResponse));

            for (TransactionEntity transactionEntity : accountResponse.getTransactions()) {
                transactions.add(transactionEntity.toTransaction());
            }

            statusUpdater.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        } while (!isContentWithRefresh(account, transactions) && accountResponse.isMoreTransactions());

        return transactions;
    }

    /**
     * Hack to filter out transactions that are booked as settled and pending at the same time.
     */
    private static List<Transaction> filterFauxDoubleCharges(List<Transaction> transactions) {
        final HashFunction hashFunction = Hashing.md5();
        final Set<HashCode> settledTransactionHashes = Sets.newHashSet();

        for (Transaction transaction : transactions) {
            if (transaction.isPending()) {
                continue;
            }

            HashCode settledTransactionHash = getTransactionHash(hashFunction, transaction);
            settledTransactionHashes.add(settledTransactionHash);
        }

        return transactions.stream().filter(transaction -> {
            if (!transaction.isPending()) {
                return true;
            }

            HashCode transactionHash = getTransactionHash(hashFunction, transaction);
            return !settledTransactionHashes.contains(transactionHash);
        }).collect(Collectors.toList());
    }

    private String requestChallengeResponse(String challenge) {
        log.info("Requesting sändkod from user with skärmkod: " + challenge);

        List<Field> fields = getChallengeSupplementalFields(challenge);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = supplementalRequester.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        return getChallengeSupplementalResponse(supplementalInformation);
    }

    private List<Field> getChallengeSupplementalFields(String challenge) {
        Field challengeField = new Field();

        challengeField.setImmutable(true);
        challengeField.setDescription("Skärmkod");
        challengeField.setValue(challenge);
        challengeField.setName("challenge");
        challengeField.setHelpText("Skriv skärmkoden i din säkerhetsdosa för att skapa en sändkod");

        Field responseField = new Field();

        responseField.setDescription("Sändkod");
        responseField.setName("response");
        responseField.setNumeric(true);
        responseField.setHint("(6-8 siffror)");
        responseField.setMaxLength(8);
        responseField.setMinLength(6);
        responseField.setPattern("([0-9]{6}|[0-9]{8})");

        return Lists.newArrayList(challengeField, responseField);
    }

    private String getChallengeSupplementalResponse(String supplementalInformation) {
        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers = SerializationUtils.deserializeFromString(supplementalInformation,
                new TypeReference<HashMap<String, String>>() {
                });

        return answers.get("response");
    }

    private enum UserMessage implements LocalizableEnum {
        SERVICE_CODE_BLOCKED(new LocalizableKey("You have enter incorrect service code to many times. For safety reasons the service code is now blocked. You may unlock your safety in Hembanken, in Mobila tjänster.")),
        NO_SERVICE_CODE(new LocalizableKey("To use the mobile services you need to sign an agreement for Service code. Log in at Hembanken and and look in Mobila tjänster - Se och ändra servicekod.")),
        PERSONAL_NUMBER_SERVICE_CODE(new LocalizableKey("Wrong personal number or service code. Please try again or control your service code in Hembanken, in Mobila tjänster."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }
        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
