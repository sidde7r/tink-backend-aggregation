package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
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
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.CreateSessionResponse;
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
import se.tink.backend.aggregation.agents.banks.danskebank.v2.util.DanskeBankDateUtil;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.SwedishGiroType;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    PAYMENTS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS
})
public final class DanskeBankV2Agent extends AbstractAgent
        implements RefreshEInvoiceExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor,
                TransferExecutor {

    private final Client httpClient;

    enum TransactionType {
        FUTURE,
        NORMAL
    }

    private static final DanskeBankAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new DanskeBankAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG =
            TransferMessageLengthConfig.createWithMaxLength(19, 12);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Don't change! (requires migration)
    private static final String SENSITIVE_PAYLOAD_SECURITY_KEY = "securityKey";
    private static final int OK_STATUS_CODE = 0;

    private final DanskeBankApiClient apiClient;
    private final Credentials credentials;
    private final String loginId;
    private final Catalog catalog;
    private final BankIdResourceHelper bankIdResourceHelper;
    private final TransferMessageFormatter transferMessageFormatter;
    private static final int MAX_ATTEMPTS = 90;

    private Map<AccountEntity, Account> accountMap = null;
    private CreateSessionResponse sessionResponse;
    private final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private DanskeBankDateUtil danskeBankDateUtil;

    public DanskeBankV2Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        String providerCountry = getProviderCountry();
        String sessionLanguage = "SE";

        credentials = request.getCredentials();
        loginId = getLoginIdFromCredentials(providerCountry, credentials);
        danskeBankDateUtil = new DanskeBankDateUtil(DEFAULT_ZONE_ID, DEFAULT_LOCALE);

        if (Objects.equals(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
            bankIdResourceHelper = new BankIdResourceHelper(credentials);
        } else {
            bankIdResourceHelper = null;
        }

        httpClient = clientFactory.createBasicClient(context.getLogOutputStream());
        this.apiClient =
                new DanskeBankApiClient(
                        httpClient,
                        CommonHeaders.DEFAULT_USER_AGENT,
                        bankIdResourceHelper,
                        providerCountry,
                        sessionLanguage,
                        credentials);
        catalog = context.getCatalog();
        transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(",._-?!/:()&`~"));
    }

    private String getProviderCountry() {
        return request.getProvider().getPayload();
    }

    private static String getLoginIdFromCredentials(
            String providerCountry, Credentials credentials) {
        if (Objects.equals(providerCountry, "SE")) {
            return credentials.getUsername().substring(2);
        }

        return credentials.getUsername();
    }

    private static Optional<TransferAccountEntity> findAccount(
            AccountIdentifier identifier, List<TransferAccountEntity> transferAccountEntities) {
        final String danskeAdaptedIdentifier =
                identifier.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

        return transferAccountEntities.stream()
                .filter(
                        transferAccountEntity ->
                                Objects.equals(
                                        transferAccountEntity.getAccountNumber(),
                                        danskeAdaptedIdentifier))
                .findFirst();
    }

    private static HashCode getTransactionHash(HashFunction hashFunction, Transaction transaction) {
        return hashFunction
                .newHasher()
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
                default:
                    throw new IllegalStateException(
                            "Should never happen, not recognized transfer type");
            }
        } catch (BankIdException bankIdException) {
            throw toTransferExecutionException(bankIdException);
        }
    }

    private TransferExecutionException toTransferExecutionException(
            BankIdException bankIdException) {
        BankIdResponse bankIdResponse = bankIdException.getResponse();
        String bankIDStatusCode = bankIdResponse.getBankIdStatusCode();

        if (bankIdResponse.isWaitingForUserInput() || bankIdResponse.isTimeout()) {
            return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
                    .setMessage(
                            bankIDStatusCode
                                    + " - Timed out or did not open app: Failed to sign transfer with BankID")
                    .setInternalStatus(InternalStatus.BANKID_NO_RESPONSE.toString())
                    .build();
        } else if (bankIdResponse.isUserCancelled()) {
            return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                    .setMessage(
                            bankIDStatusCode
                                    + " - User cancelled: Failed to sign transfer with BankID")
                    .setInternalStatus(InternalStatus.BANKID_CANCELLED.toString())
                    .build();
        } else {
            String logMessage = bankIDStatusCode + " - Failed to sign transfer with BankID";
            return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage
                                            .BANKID_TRANSFER_FAILED))
                    .setMessage(logMessage)
                    .build();
        }
    }

    private void executeBankTransfer(final Transfer transfer)
            throws IOException, BankIdException, AuthenticationException {
        TransferDetailsResponse detailsResponse = apiClient.getTransferAccounts();

        AccountIdentifier source = transfer.getSource();

        Optional<TransferAccountEntity> fromAccount =
                findAccount(source, detailsResponse.getFromAccounts());
        if (!fromAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                    .build();
        }

        // TODO: Adapt this to multiple markets.

        String transferDate = null;
        if (IntraBankChecker.isSwedishMarketIntraBank(
                fromAccount.get().generalGetAccountIdentifier(), transfer.getDestination())) {
            transferDate =
                    danskeBankDateUtil.getTransferDateForInternalTransfer(transfer.getDueDate());
        } else {
            transferDate =
                    danskeBankDateUtil.getTransferDateForExternalTransfer(transfer.getDueDate());
        }
        // Check if the transfer is between two accounts belonging to the same credentials.
        boolean isBetweenUserAccounts =
                findAccount(transfer.getDestination(), detailsResponse.getFromAccounts())
                        .isPresent();

        TransferRequest transferRequest =
                createTransferRequest(
                        transfer, fromAccount.get(), transferDate, isBetweenUserAccounts);
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

        if (transfer.getRemittanceInformation().getType() == null) {
            RemittanceInformation remittanceInformation = transfer.getRemittanceInformation();
            remittanceInformation.setType(decideRemittanceInformationType(remittanceInformation));
        }

        Optional<TransferAccountEntity> fromAccount =
                findAccount(source, detailsResponse.getFromAccounts());
        if (!fromAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                    .build();
        }

        BillRequest billRequest = createBillRequest(transfer, fromAccount.get());
        BillResponse billResponse = apiClient.createPayment(billRequest);

        if (billResponse.isBillNotValid()) {
            String errorMessage =
                    billResponse.getText() != null
                            ? billResponse.getText()
                            : catalog.getString(
                                    TransferExecutionException.EndUserMessage
                                            .PAYMENT_CREATE_FAILED);
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            "Danskebank rejects the bill because it is invalid according to Danskebank.")
                    .setEndUserMessage(errorMessage)
                    .build();
        } else if (!billResponse.isChallengeNeeded()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No challenge required for DanskeBank payment!")
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage
                                            .PAYMENT_CREATE_FAILED))
                    .build();
        }

        apiClient.confirmPayment(requestChallenge(billResponse));
    }

    private RemittanceInformationType decideRemittanceInformationType(
            RemittanceInformation remittanceInformation) {
        return isValidSoftOcr(remittanceInformation.getValue())
                ? RemittanceInformationType.OCR
                : RemittanceInformationType.UNSTRUCTURED;
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
            String challengeResponse =
                    Optional.ofNullable(requestChallengeResponse(challenge.getChallenge()))
                            .orElseThrow(
                                    () ->
                                            TransferExecutionException.builder(
                                                            SignableOperationStatuses.CANCELLED)
                                                    .setEndUserMessage(
                                                            catalog.getString(
                                                                    TransferExecutionException
                                                                            .EndUserMessage
                                                                            .CHALLENGE_NO_RESPONSE))
                                                    .setMessage(
                                                            "Failed to get challenge response from user")
                                                    .setInternalStatus(
                                                            InternalStatus
                                                                    .SECURITY_TOKEN_NO_RESPONSE
                                                                    .toString())
                                                    .build());

            challengeResponseRequest.setResponse(challengeResponse);
        }

        return challengeResponseRequest;
    }

    private String getOrderReferenceFromChallenge(AbstractChallengeResponse challenge)
            throws IOException {
        MessageContainer challengeContainer =
                MAPPER.readValue(challenge.getChallenge(), MessageContainer.class);
        SignBankIdResponse signBankIdResponse =
                challengeContainer.decrypt(bankIdResourceHelper, SignBankIdResponse.class);

        return signBankIdResponse.getOrderReference();
    }

    private TransferRequest createTransferRequest(
            Transfer transfer,
            TransferAccountEntity fromAccount,
            String transferDate,
            boolean isBetweenUserAccounts)
            throws TransferMessageException {

        String destinationAccountId =
                transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

        String formattedDestinationMessage =
                transferMessageFormatter.getDestinationMessageFromRemittanceInformation(
                        transfer, isBetweenUserAccounts);
        String formattedSourceMessage = transferMessageFormatter.getSourceMessage(transfer);

        return TransferRequest.builder()
                .amount(Double.toString(transfer.getAmount().getValue()))
                .currency(request.getProvider().getCurrency()) // Is this valid?
                .date(transferDate)
                .destinationAccountNumber(destinationAccountId)
                .destinationMessage(formattedDestinationMessage)
                .sourceAccountNumber(fromAccount.getAccountId())
                .sourceMessage(formattedSourceMessage)
                .build();
    }

    private BillRequest createBillRequest(Transfer transfer, TransferAccountEntity fromAccount) {
        String destinationAccountId =
                transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);

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

        if (transfer.getRemittanceInformation().getType() == RemittanceInformationType.OCR) {
            billRequest.setReference(transfer.getRemittanceInformation().getValue());
        } else {
            billRequest.setReceiverText(transfer.getRemittanceInformation().getValue());
        }

        billRequest.setDate(danskeBankDateUtil.getTransferDateForBgPg(transfer.getDueDate()));

        return billRequest;
    }

    private boolean isValidSoftOcr(String message) {
        OcrValidationConfiguration validationConfiguration = OcrValidationConfiguration.softOcr();
        GiroMessageValidator validator = GiroMessageValidator.create(validationConfiguration);

        return validator.validate(message).getValidOcr().isPresent();
    }

    private Map<Account, List<TransferDestinationPattern>> getBankTransferAccountDestinations(
            List<Account> updatedAccounts) {
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
            String bankIdStatusCode = bankIdResponse.getBankIdStatusCode();
            String bankIdStatusText = bankIdResponse.getBankIdStatusText();

            if (bankIdResponse.isAlreadyInProgress()) {
                log.info(
                        String.format(
                                "Status code: %s, status text: %s, interpreted as bankID already in progress.",
                                bankIdStatusCode, bankIdStatusText),
                        bankIdException);
                throw BankIdError.ALREADY_IN_PROGRESS.exception(bankIdException);
            }

            if (bankIdResponse.isUserCancelled()) {
                log.info(
                        bankIdStatusCode + " - User cancelled BankID authentication",
                        bankIdException);
                throw BankIdError.CANCELLED.exception(bankIdException);
            }

            if (bankIdResponse.isTimeout() || bankIdResponse.isWaitingForUserInput()) {
                log.info(
                        bankIdStatusCode + " - User timeout authenticating with BankID",
                        bankIdException);
                throw BankIdError.TIMEOUT.exception(bankIdException);
            }

            throw new IllegalStateException(
                    String.format(
                            "#danskebankV2 - BankID authentication failed with status code: %s, and status text: %s",
                            bankIdStatusCode, bankIdStatusText),
                    bankIdException);
        }
    }

    private void loginWithMobileBankId() throws BankIdException, LoginException, SessionException {
        InitBankIdRequest initBankIdRequest = new InitBankIdRequest(loginId);
        InitBankIdResponse initBankIdResponse = apiClient.bankIdInitAuth(initBankIdRequest);

        supplementalRequester.openBankId();

        String orderReference = initBankIdResponse.getOrderReference();
        verifyBankId(BankIdServiceType.VERIFYAUTH, orderReference);

        sessionResponse = apiClient.createAuthenticatedSession();
    }

    private void verifyBankId(BankIdServiceType bankIdServiceType, String orderReference)
            throws BankIdException, LoginException {
        BankIdResponse bankIdResponse = null;

        // Solves a bug with a SocketTimeoutException below in postBankIdService() due to calling
        // this too quickly.
        Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            bankIdResponse = apiClient.bankIdVerify(bankIdServiceType, orderReference);

            if (bankIdResponse.isUserAuthenticated()) {
                log.info(
                        bankIdServiceType.toString()
                                + " - User authenticated successfully with BankID");
                return;
            } else if (!bankIdResponse.isWaitingForUserInput()) {
                throw new BankIdException(bankIdResponse);
            }

            log.info(
                    bankIdServiceType.toString()
                            + " - Waiting for user to authenticate using BankID");
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw new BankIdException(bankIdResponse);
    }

    private boolean loginWithPassword() throws AuthenticationException, AuthorizationException {
        sessionResponse = apiClient.initAndCreateSession();

        LoginResponse loginResponse = apiClient.loginWithPassword(createLoginRequest());
        String generalLoginErrorMessage = catalog.getString("Could not login, please try again.");

        if (loginResponse.isChallengeNeeded()) {
            if (!request.isManual()) {
                log.warn("Login requires supplemental information, but the request is not manual.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            credentials.setSensitivePayload(
                    SENSITIVE_PAYLOAD_SECURITY_KEY, loginResponse.getSecurityKey());

            String challengeResponse = requestChallengeResponse(loginResponse.getChallenge());

            if (Strings.isNullOrEmpty(challengeResponse)) {
                handleNullChallengeResponseForLogin(loginResponse);
            }

            apiClient.loginConfirmChallenge(loginResponse, challengeResponse);
        }

        int statusCode = loginResponse.getStatus().getStatusCode();
        String errorMessage =
                loginResponse.getStatus().getStatusText() != null
                        ? loginResponse.getStatus().getStatusText()
                        : generalLoginErrorMessage;

        switch (statusCode) {
            case 0:
                return true;
            case 1:
                if (errorMessage.toLowerCase().contains("fel personnummer eller servicekod")) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(
                            UserMessage.PERSONAL_NUMBER_SERVICE_CODE.getKey());
                } else if (errorMessage.toLowerCase().contains("teckna avtal om Servicekod")) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            UserMessage.NO_SERVICE_CODE.getKey());
                }
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Login failed with statusCode %s, statusText %s",
                                statusCode, errorMessage));
            case 3:
                if (errorMessage
                        .toLowerCase()
                        .contains("du har angivit fel servicekod för många gånger")) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                            UserMessage.SERVICE_CODE_BLOCKED.getKey());
                }
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Login failed with statusCode %s, statusText %s",
                                statusCode, errorMessage));
            case 4:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Login failed with statusCode %s, statusText %s",
                                statusCode, errorMessage));
                // Looks like there are only when there is some problem at Danskes side, see comment
                // in: https://github.com/tink-ab/tink-backend/pull/5398
                // Not implemented yet.
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Login failed with statusCode %s, statusText %s",
                                statusCode, errorMessage));
        }
    }

    private void handleNullChallengeResponseForLogin(LoginResponse loginResponse)
            throws SupplementalInfoException {

        // If the login response is OK, then the user should have received the challenge. If the
        // challenge response is null then it's because the user didn't input anything.
        if (loginResponse.isStatusOk()) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        throw new IllegalStateException(
                String.format(
                        "#login-refactoring - Login failed with null/empty challenge response, statusCode %s, statusText %s.",
                        loginResponse.getStatus().getStatusCode(),
                        loginResponse.getStatus().getStatusText()));
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

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(
            List<Account> updatedAccounts) {
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
            destinationAccounts.addAll(
                    toAccountsBankGiro.stream()
                            .map(tae -> new PaymentAccountWrapper(tae, SwedishGiroType.BG))
                            .collect(Collectors.toList()));
        }

        List<TransferAccountEntity> toAccountsPlusGiro = detailsResponse.getToAccountsPlusGiro();
        if (toAccountsPlusGiro != null) {
            destinationAccounts.addAll(
                    toAccountsPlusGiro.stream()
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

    private List<Transaction> fetchTransactions(
            AccountEntity accountEntity, Account account, TransactionType type) {
        List<Transaction> transactions = Lists.newArrayList();

        AccountResponse accountResponse = null;
        do {
            accountResponse =
                    apiClient.getTransactions(
                            accountEntity, type, Optional.ofNullable(accountResponse));

            for (TransactionEntity transactionEntity : accountResponse.getTransactions()) {
                transactions.add(transactionEntity.toTransaction());
            }

            statusUpdater.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        } while (!isContentWithRefresh(account, transactions)
                && accountResponse.isMoreTransactions());

        return transactions;
    }

    /** Hack to filter out transactions that are booked as settled and pending at the same time. */
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

        return transactions.stream()
                .filter(
                        transaction -> {
                            if (!transaction.isPending()) {
                                return true;
                            }

                            HashCode transactionHash =
                                    getTransactionHash(hashFunction, transaction);
                            return !settledTransactionHashes.contains(transactionHash);
                        })
                .collect(Collectors.toList());
    }

    private String requestChallengeResponse(String challenge) {
        log.info("Requesting sändkod from user with skärmkod: " + challenge);

        List<Field> fields = getChallengeSupplementalFields(challenge);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation =
                supplementalRequester.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        return getChallengeSupplementalResponse(supplementalInformation);
    }

    private List<Field> getChallengeSupplementalFields(String challenge) {
        Field challengeField =
                Field.builder()
                        .immutable(true)
                        .description("Skärmkod")
                        .value(challenge)
                        .name("challenge")
                        .helpText("Skriv skärmkoden i din säkerhetsdosa för att skapa en sändkod")
                        .build();

        Field responseField =
                Field.builder()
                        .description("Sändkod")
                        .name("response")
                        .numeric(true)
                        .hint("(6-8 siffror)")
                        .maxLength(8)
                        .minLength(6)
                        .pattern("([0-9]{6}|[0-9]{8})")
                        .build();

        return Lists.newArrayList(challengeField, responseField);
    }

    private String getChallengeSupplementalResponse(String supplementalInformation) {
        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers =
                SerializationUtils.deserializeFromString(
                        supplementalInformation, new TypeReference<HashMap<String, String>>() {});

        return answers.get("response");
    }

    private enum UserMessage implements LocalizableEnum {
        SERVICE_CODE_BLOCKED(
                new LocalizableKey(
                        "You have enter incorrect service code to many times. For safety reasons the service code is now blocked. You may unlock your safety in Hembanken, in Mobila tjänster.")),
        NO_SERVICE_CODE(
                new LocalizableKey(
                        "To use the mobile services you need to sign an agreement for Service code. Log in at Hembanken and and look in Mobila tjänster - Se och ändra servicekod.")),
        PERSONAL_NUMBER_SERVICE_CODE(
                new LocalizableKey(
                        "Wrong personal number or service code. Please try again or control your service code in Hembanken, in Mobila tjänster."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    //// Refresh Executor Refactor ///////
    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        try {

            List<Transfer> eInvoices = Lists.newArrayList();

            EInvoiceListResponse eInvoiceList = apiClient.getEInvoices();

            for (EInvoiceListTransactionEntity eInvoiceListTransactionEntity :
                    eInvoiceList.getTransactions()) {
                String transferId = eInvoiceListTransactionEntity.getTransactionId();

                EInvoiceDetailsResponse eInvoiceDetails = apiClient.getEInvoiceDetails(transferId);

                // Old eInvoices have the status invalid, ignore those
                if (eInvoiceDetails.isInvalidEInvoice()) {
                    continue;
                }

                try {
                    // There's an identifier on the list response, add that id as payload for
                    // identification of invoice
                    Transfer eInvoiceTransfer = eInvoiceDetails.toEInvoiceTransfer(transferId);
                    eInvoices.add(eInvoiceTransfer);
                } catch (Exception e) {
                    log.warn("Validation failed when trying to save e-invoice", e);
                }
            }
            return new FetchEInvoicesResponse(eInvoices);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {

        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();

        transferDestinations.putAll(getBankTransferAccountDestinations(accounts));

        Map<Account, List<TransferDestinationPattern>> paymentDestinations =
                getPaymentAccountDestinations(accounts);

        for (Map.Entry<Account, List<TransferDestinationPattern>> entry :
                paymentDestinations.entrySet()) {
            // if account exists in response, add payment destinations to already added transfer
            // destinations
            if (transferDestinations.containsKey(entry.getKey())) {
                transferDestinations.get(entry.getKey()).addAll(entry.getValue());
            } else {
                // otherwise add a new entry
                transferDestinations.put(entry.getKey(), entry.getValue());
            }
        }

        return new FetchTransferDestinationsResponse(transferDestinations);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return fetchAccountsPerType(RefreshableItem.CHECKING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return fetchTransactionsPerType(RefreshableItem.CHECKING_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return fetchAccountsPerType(RefreshableItem.CREDITCARD_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return fetchTransactionsPerType(RefreshableItem.CREDITCARD_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return fetchAccountsPerType(RefreshableItem.SAVING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return fetchTransactionsPerType(RefreshableItem.SAVING_TRANSACTIONS);
    }

    private FetchAccountsResponse fetchAccountsPerType(RefreshableItem type) {
        List<Account> accounts = new ArrayList<>();
        getAccountMap().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> accounts.add(set.getValue()));
        return new FetchAccountsResponse(accounts);
    }

    private FetchTransactionsResponse fetchTransactionsPerType(RefreshableItem type) {
        Map<Account, List<Transaction>> transactionMap = new HashMap<>();
        getAccountMap().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(
                        set -> {
                            AccountEntity accountEntity = set.getKey();
                            boolean isCreditCardAccount =
                                    apiClient.isCreditCardAccount(accountEntity);
                            Account account = accountEntity.toAccount(isCreditCardAccount);

                            List<Transaction> transactions = Lists.newArrayList();

                            transactions.addAll(
                                    fetchTransactions(
                                            accountEntity, account, TransactionType.FUTURE));
                            transactions.addAll(
                                    fetchTransactions(
                                            accountEntity, account, TransactionType.NORMAL));

                            transactions = filterFauxDoubleCharges(transactions);

                            transactionMap.put(account, transactions);
                        });
        return new FetchTransactionsResponse(transactionMap);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        try {
            return fetchInvestmentData();
        } catch (Exception e) {
            // Catch and just exit - This is not yet implemented in our model.
            log.warn("Caught exception while logging investment data", e);
            return new FetchInvestmentAccountsResponse(Collections.emptyMap());
        }
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    private FetchInvestmentAccountsResponse fetchInvestmentData() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        PortfoliosListResponse portfoliosListResponse = apiClient.getPortfolios();

        if (portfoliosListResponse.getStatus().getStatusCode() != OK_STATUS_CODE
                || portfoliosListResponse.getPortfolios() == null
                || portfoliosListResponse.getPortfolios().isEmpty()) {
            return new FetchInvestmentAccountsResponse(Collections.emptyMap());
        }

        portfoliosListResponse
                .getPortfolios()
                .forEach(
                        portfolioEntity -> {
                            Account account = portfolioEntity.toAccount();
                            Portfolio portfolio = portfolioEntity.toPortfolio();

                            PapersListResponse portfolioPapers =
                                    apiClient.getPortfolioPapers(portfolioEntity.getPortfolioId());

                            if (portfolioPapers.getStatus().getStatusCode() != OK_STATUS_CODE
                                    || portfolioPapers.getPapers() == null) {
                                accounts.put(
                                        account, AccountFeatures.createForPortfolios(portfolio));
                            }

                            List<Instrument> instruments = Lists.newArrayList();
                            portfolioPapers
                                    .getPapers()
                                    .forEach(
                                            paperEntity ->
                                                    paperEntity
                                                            .toInstrument()
                                                            .ifPresent(instruments::add));
                            portfolio.setInstruments(instruments);

                            accounts.put(account, AccountFeatures.createForPortfolios(portfolio));
                        });
        return new FetchInvestmentAccountsResponse(accounts);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        getAccountMap().entrySet().stream()
                .filter(
                        set ->
                                RefreshableItem.LOAN_ACCOUNTS.isAccountType(
                                        set.getValue().getType()))
                .forEach(
                        set ->
                                accounts.put(
                                        set.getValue(),
                                        AccountFeatures.createForLoan(set.getKey().toLoan())));
        return new FetchLoanAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        if (sessionResponse == null) {
            throw new NoSuchElementException("SessionResponse is null.");
        }

        String name = "";

        if (sessionResponse.getLoginInfo() != null) {

            name = Strings.nullToEmpty(sessionResponse.getLoginInfo().getName());
        } else {
            log.warn("SessionResponse.LoginInfo is null.");
        }

        try {
            return new FetchIdentityDataResponse(
                    SeIdentityData.of(name, credentials.getField(Key.USERNAME)));
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to parse SSN: " + credentials.getField(Key.USERNAME), e);

            return new FetchIdentityDataResponse(
                    IdentityData.builder().setFullName(name).setDateOfBirth(null).build());
        }
    }

    //////////////////////////////////////
}
