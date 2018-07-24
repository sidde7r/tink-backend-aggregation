package se.tink.backend.aggregation.agents.banks.handelsbanken.v6;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.IOException;
import java.security.Security;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.joda.time.LocalDate;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AbstractRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AbstractResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountGroupEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AccountListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ApproveEInvoiceRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ApproveEInvoicesResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AuthenticateBankIdResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AuthorizeBankIdResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.AuthorizeResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CallengeResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CardEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CardListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CardTransactionListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CheckAgreementResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CommitProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CreateProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CustodyAccountEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CustodyAccountResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.CustodyHoldingsEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.EInvoicesGroupedEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.EntrypointsResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.FundAccountHoldingDetailResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.FundHoldingsResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HandshakeResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HoldingsListsEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.HoldingsSummaryResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.InitBankIdRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.InitNewProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LinkEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LoanEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.LoansResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PaymentContextResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PendingEInvoicesResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.PensionDetailsResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.RecipientAccountEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ServerProfileResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.SignEInvoicesResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransferContextResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransferRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.TransferResponseEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.UpdatePaymentRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ValidRecipientEntity;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.tfa.LibTFA;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.SHBUtils;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.Session;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Random information:
 *
 * a) Maximum of 14 characters in message when transferring
 *
 * b) Maximum of 12 characters in message when transferring to other bank
 *
 */
public class HandelsbankenV6Agent extends AbstractAgent
        implements RefreshableItemExecutor, TransferExecutor, PersistentLogin {

    public static final String ACCOUNT_GROUP_TYPE_OTHER = "OTHER_RECIPIENT_ACCOUNT";
    public static final String ACCOUNT_GROUP_TYPE_OWN = "OWN_ACCOUNT";

    private static final String BASE_URL = "https://m2.handelsbanken.se";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 65;
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");
    private static final String REL_ACCOUNTS = "accounts";
    private static final String REL_CARDS = "cards";
    private static final String REL_CARDS_V2 = "cards-v2";
    private static final String REL_LOANS = "loans";
    private static final String REL_BANK_TRANSFER_CONTEXT = "transfer-context";
    private static final String REL_PAYMENT_CONTEXT = "payment-context";
    private static final String REL_TRANSFER_RECIPIENT = "validate-recipient";
    private static final String REL_TRANSFER_CREATE = "create";
    private static final String REL_TRANSFER_SIGN = "signature";
    private static final String REL_PENDING_PAYMENTS = "pending-transactions";
    private static final String REL_PENDING_PAYMENTS_V3 = "pending-transactions-v3";
    private static final String REL_LOOKUP_RECIPIENT = "lookup-recipient";
    private static final String REL_PENDING_EINVOICES = "einvoice-pending-v3";
    private static final String REL_EINVOICE_APPROVAL = "approval";
    private static final String REL_EINVOICE_SIGNATURE = "signature";
    private static final String REL_PAYMENT_DETAILS = "payment-detail";
    private static final String REL_EINVOICE_DETAILS = "einvoice-detail";
    private static final String REL_UDPATE = "update";
    private static final String REL_SECURITIES_HOLDINGS = "securities-holdings-v2";
    private static final String REL_FUND_HOLDINGS = "fund-holdings";
    private static final String REL_PENSION_DETAILS = "pension-detail";
    private static final String REL_FUND_HOLDING_DETAILS = "fund-holding-details";
    private static final String REL_CUSTODY_ACCOUNTS = "custody-account";
    private static final String REL_SECURITY_HOLDING = "security-holding";

    private static final String BANKID_CANCELLED_CODE = "111";
    private static final String BANKID_TIMEOUT_CODE = "110";
    private static final String BANKID_AUTH_ERROR_CODE = "102";

    private static final LogTag LOG_TAG_PENSION_DETAILS = LogTag.from("#handelsbanken_pension_details");

    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig
            .createWithMaxLength(14, 12);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    // caching
    private Map<AccountEntity, Account> accountEntityAccountMap = null;
    private Map<CardEntity, Account> cardEntityAccountMap = null;
    private List<LoanEntity> loans = null;
    private boolean hasRefreshedTransactions = false;

    /**
     * Helper method to find REST HREF links.
     */
    private static LinkEntity findLinkEntity(List<LinkEntity> links, final String rel) {
        return SHBUtils.findLinkEntity(links, rel).orElse(null);
    }

    private static LinkEntity findLinkEntity(Map<String, LinkEntity> links, final String rel) {
        return SHBUtils.findLinkEntity(links, rel).orElse(null);
    }

    /*
        [2017-01-11] jrenold:
            An attempt to support the new endpoints. The data returned from the new endpoints are, at this date,
            identical to the current (v1) endpoints. Please note though that the endpoint 'cards-v2' has not been
            fully tested internally, no Tink employees have credit cards @ SHB.
            This is not a bullet proof solution; we cannot know if the new endpoints will be the same once the
            old endpoints are gone.

            1. Enable the new endpoints for feature flagged users.
            2. Switch to the new endpoints in case the old ones are removed.
            3. Log when the old endpoints are removed from the SHB backend.
            4. All "REL_*" key names are now monitored in the AppStoreMonitor.
     */
    private LinkEntity findLinkEntityNewApi(Map<String,LinkEntity> links, final String relOld, final String relNew) {

        if (request.getUser().getFlags().contains(FeatureFlags.TEMP_SHB_NEW_API)) {
            // Always use the new endpoint for feature flagged users
            return findLinkEntity(links, relNew);
        }

        LinkEntity link = findLinkEntity(links, relOld);
        if (link == null) {
            // log that the old endpoint has been removed
            log.info(String.format("[Handelsbanken] Old endpoint \"%s\" has been removed.", relOld));

            link = findLinkEntity(links, relNew);
        }
        return link;
    }

    private TinkApacheHttpClient4 client;
    private HandelsbankenAPIAgent apiClient;
    private EntrypointsResponse entrypointsResponse;
    private Session session;
    private final Credentials credentials;
    private final TransferMessageFormatter transferMessageFormatter;

    public HandelsbankenV6Agent(CredentialsRequest request, AgentContext context) {
        super(request, context);

        credentials = request.getCredentials();

        initClient();
        transferMessageFormatter = new TransferMessageFormatter(context.getCatalog(),
                TRANSFER_MESSAGE_LENGTH_CONFIG,
                new StringNormalizerSwedish(",.-?!/+"));
    }

    private WebResource.Builder createClientRequest(String url) {
        return apiClient.createClientRequest(url);
    }

    private void initClient() {
        client = clientFactory.createCustomClient(context.getLogOutputStream());
        client.setFollowRedirects(true);
//        client.addFilter(new LoggingFilter(new PrintStream(System.out, true)));

        apiClient = new HandelsbankenAPIAgent(client, getAggregator().getAggregatorIdentifier());
    }

    /**
     * Activate a new device profile using the keyfob. Stores the resulting private key and profile id into the
     * credentials payload.
     */
    private EntrypointsResponse activate(boolean isSecondTry) throws LoginException, AuthorizationException {
        // Don't allow automatic updates to initiate activations.

        // TODO: Replace with new AgentWorker
        if (!request.isUpdate() && !request.isCreate()) {
            // if (!(request instanceof UpdateCredentialsRequest) && !(request instanceof CreateCredentialsRequest)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.ACTIVATION_NEEDED.getKey());
        }

        Credentials credentials = request.getCredentials();
        LibTFA tfa = new LibTFA(credentials.getField(Field.Key.USERNAME));

        EntrypointsResponse entrypoints = getEntryPoints();

        // Initialize new profile.

        String pinnedActivationUrl = findLinkEntity(entrypoints.getLinksMap(), "pinned-activation").getHref();

        InitNewProfileResponse initNewProfileResponse = createClientRequest(pinnedActivationUrl).type(
                MediaType.APPLICATION_JSON).post(InitNewProfileResponse.class, tfa.createInitNewProfileRequest());

        tfa.handleInitNewProfileResponse(initNewProfileResponse);

        // Create the MFA fields to be sent to the client.

        Field challengeField = new Field();

        challengeField.setImmutable(true);
        challengeField.setDescription("Kontrollkod");
        challengeField.setValue(initNewProfileResponse.getChallenge());
        challengeField.setName("challenge");
        challengeField
                .setHelpText("Sätt i ditt inloggningskort i kortläsaren och tryck på knappen SIGN. Knappa därefter in kontrollkoden och din PIN-kod till kortet i kortläsaren. Skriv in svarskoden i fältet nedan.");

        Field responseField = new Field();

        responseField.setDescription("Svarskod");
        responseField.setName("response");
        responseField.setNumeric(true);
        responseField.setHint("NNN NNN NNN");
        responseField.setMaxLength(9);
        responseField.setMinLength(8); // Allow 8 digits, to be able to return a helpful error message for that case.
        responseField.setPattern("([0-9]{8,9})");

        List<Field> fields = Lists.newArrayList(challengeField, responseField);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        // Send the MFA challenge to the client.

        String supplementalInformation = context.requestSupplementalInformation(credentials);

        if (supplementalInformation == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.ACTIVATION_NEEDED.getKey());
        }

        Map<String, String> answers = SerializationUtils.deserializeFromString(supplementalInformation,
                new TypeReference<HashMap<String, String>>() {
                });

        String code = answers.get("response");

        if (code.length() == 8) {
            // MasterCard instead of login card has been used in the card reader
            throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.WRONG_CARD.getKey());
        }

        context.updateStatus(CredentialsStatus.AUTHENTICATING);

        // Create profile.

        String createProfileUrl = findLinkEntity(initNewProfileResponse.getLinksMap(), "createProfile").getHref();

        CreateProfileResponse createProfileResponse = createClientRequest(createProfileUrl).type(
                MediaType.APPLICATION_JSON).post(CreateProfileResponse.class, tfa.createCreateProfileRequest(code));
        tfa.handleCreateProfileResponse(createProfileResponse);

        // Activate profile.

        String activateProfileUrl = findLinkEntity(createProfileResponse.getLinksMap(), "activateProfile").getHref();

        ActivateProfileResponse activateProfileResponse = createClientRequest(activateProfileUrl).type(
                MediaType.APPLICATION_JSON).post(ActivateProfileResponse.class,
                tfa.createActivateProfileRequest());
        tfa.handleActivateProfileResponse(activateProfileResponse);

        // Commit profile.

        String commitProfileUrl = findLinkEntity(activateProfileResponse.getLinksMap(), "commitProfile").getHref();

        CommitProfileResponse commitProfileResponse = createClientRequest(commitProfileUrl).get(
                CommitProfileResponse.class);
        String statusCode = commitProfileResponse.getCode();

        if (!Strings.isNullOrEmpty(statusCode)) {
            switch (statusCode) {
            case "101":
                throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.INCORRECT_CREDENTIALS.getKey());
            case "102":
                throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.WRONG_ACTIVATION_CODE.getKey());
            case "104":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.TOO_MANY_ACTIVATED_APPS.getKey());
            case "103":
                String message = commitProfileResponse.getMessage();
                if (!Strings.isNullOrEmpty(message)) {
                    if (message.toLowerCase().contains("låst i 60 min för signering")) {
                        throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.TEMP_BLOCKED_CARD.getKey());
                    }
                    if (message.toLowerCase().contains("kortet är spärrat")) {
                        throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.PERM_BLOCKED_CARD.getKey());
                    }
                }
                // fall through
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - SHB - Login failed (commitProfileResponse) with message %s, code %s, error message %s",
                                commitProfileResponse.getMessage(),
                                statusCode,
                                commitProfileResponse.getFirstErrorMessage()));
            }
        }

        // Check agreement.

        String checkAgreementUrl = findLinkEntity(commitProfileResponse.getLinksMap(), "checkAgreement").getHref();

        CheckAgreementResponse checkAgreementResponse = createClientRequest(checkAgreementUrl).get(
                CheckAgreementResponse.class);

        if (Objects.equal("NOT EXIST", checkAgreementResponse.getResult())) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(UserMessage.CODE_ACTIVATION_NEEDED.getKey());
        } else if (!Objects.equal("EXIST", checkAgreementResponse.getResult())) {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - SHB - Login failed (checkAgreementResponse) with message %s, code %s, error message %s, result %s",
                            checkAgreementResponse.getMessage(),
                            checkAgreementResponse.getCode(),
                            checkAgreementResponse.getFirstErrorMessage(),
                            checkAgreementResponse.getResult()));
        }

        // Continue and do a regular login if we've successfully activated a new profile.

        credentials.addSensitivePayload(tfa.getPayload());

        // Files.write(SerializationUtils.serializeToString(tfa.getPayload()), new File(
        // "data/agents/shb/user1-test.payload"), Charsets.UTF_8);

        return login(isSecondTry);
    }

    private EntrypointsResponse authenticateBankId() throws BankIdException, LoginException, AuthorizationException {
        Credentials credentials = request.getCredentials();

        // Initiate a login.

        InitBankIdRequest initRequest = new InitBankIdRequest();
        initRequest.setPersonalNumber(request.getCredentials().getField(Field.Key.USERNAME));

        InitBankIdResponse initResponse = createClientRequest(BASE_URL + "/bb/gls3/aa/privmobbidse/init/2.0")
                .type(MediaType.APPLICATION_JSON).post(InitBankIdResponse.class, initRequest);
        String initCode = initResponse.getCode();

        if (Objects.equal(initCode, BANKID_AUTH_ERROR_CODE)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (Objects.equal(initCode, BANKID_CANCELLED_CODE)) {
            //Åtgärden avbruten. Försök igen
            initResponse = createClientRequest(BASE_URL + "/bb/gls3/aa/privmobbidse/init/2.0")
                    .type(MediaType.APPLICATION_JSON).post(InitBankIdResponse.class, initRequest);
        }

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        context.requestSupplementalInformation(credentials, false);

        // Wait for authentication is successful.

        LinkEntity authenticateLink = findLinkEntity(initResponse.getLinksMap(), "authenticate");

        AuthenticateBankIdResponse authenticateResponse = null;
        String authenticateResult = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            authenticateResponse = createClientRequest(authenticateLink.getHref()).type(
                    MediaType.APPLICATION_JSON).post(AuthenticateBankIdResponse.class, new AbstractRequest());

            authenticateResult = authenticateResponse.getResult();

            if (!Strings.isNullOrEmpty(authenticateResult)) {
                if (Objects.equal(authenticateResult.toUpperCase(), "AUTHENTICATED")) {
                    break;
                }

                if (Objects.equal(authenticateResult.toUpperCase(), "MUST_ACTIVATE")) {
                    log.info("Got MUST_ACTIVATE");
                    throw LoginError.INCORRECT_CREDENTIALS.exception(
                            UserMessage.BANKID_ACTIVATION_NEEDED.getKey());
                }
            }

            String statusCode = authenticateResponse.getCode();

            if (!Strings.isNullOrEmpty(statusCode)) {
                log.info(String.format("BankID authentication failed (code=%s, message=%s)", statusCode,
                        authenticateResponse.getMessage()));
                if (Objects.equal(statusCode.toUpperCase(), BANKID_CANCELLED_CODE)) {
                    throw BankIdError.CANCELLED.exception();
                } else if (Objects.equal(statusCode.toUpperCase(), BANKID_TIMEOUT_CODE)) {
                    throw BankIdError.TIMEOUT.exception();
                }
                break;
            }

            log.debug("Waiting for BankID: " + authenticateResponse.getResult());

            // Wait for 2 seconds before trying again (if you have any attempts left).
            if (i < (MAX_ATTEMPTS - 1)) {
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            }
        }

        if (Strings.isNullOrEmpty(authenticateResult) ||
                !Objects.equal(authenticateResult.toUpperCase(), "AUTHENTICATED")) {

            log.info(String.format("Did not get AUTHENTICATED status in response. (result=%s, code=%s, message=%s)",
                    authenticateResponse.getResult(), authenticateResponse.getCode(),
                    authenticateResponse.getMessage()));

            throw BankIdError.TIMEOUT.exception();
        }

        // Execute the authorization request.

        LinkEntity authorizeLink = findLinkEntity(authenticateResponse.getLinksMap(), "authorize");

        ClientResponse authorizeClientResponse = createClientRequest(authorizeLink.getHref()).type(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, new AbstractRequest());

        AuthorizeBankIdResponse authorizeResponse = authorizeClientResponse.getEntity(AuthorizeBankIdResponse.class);

        if (!Strings.isNullOrEmpty(authorizeResponse.getCode())) {
            switch (authorizeResponse.getCode()) {
                case "104":
                    throw AuthorizationError.UNAUTHORIZED.exception(UserMessage.PERMISSION_DENIED.getKey());
                default:
                    log.warn(String.format(
                                    "#login-refactoring - SHB - Login failed (authorizeResponse) with message %s, code %s, error message %s",
                                    authorizeResponse.getMessage(),
                                    authorizeResponse.getCode(),
                                    authorizeResponse.getFirstErrorMessage()));
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }

        LinkEntity sessionLink = findLinkEntity(authorizeResponse.getLinksMap(), "application-entry-point");

        String sessionLinkHref = sessionLink.getHref();

        // Persist the cookies and url for the authorization request so that we can use them later
        this.session = new Session();
        this.session.setSessionUrl(sessionLinkHref);

        ClientResponse response = createClientRequest(sessionLinkHref).get(ClientResponse.class);
        EntrypointsResponse entrypointsResponse = response.getEntity(EntrypointsResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            Optional<String> message = entrypointsResponse.getFirstErrorMessage();

            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - SHB - Message: %s, Code: %s, Status: %s",
                            message.orElse(null),
                            entrypointsResponse.getCode(),
                            response.getStatus()));
        }
        return entrypointsResponse;
    }

    private boolean isAlive(Session session) throws Exception {
        String errorMessage;

        if (session == null) {
            errorMessage = MoreObjects.toStringHelper(Session.class)
                    .add("session", null)
                    .toString();
        } else {
            Builder clientRequest = createClientRequest(session.getSessionUrl());

            ClientResponse clientResponse = clientRequest.get(ClientResponse.class);

            // Verifies that the status is 200 OK and that we get a response that is compatible with "application/json"
            // (Equals can not be used since charset is part of the content-type)
            if (clientResponse.getType().isCompatible(MediaType.APPLICATION_JSON_TYPE) && clientResponse.hasEntity()) {
                // important that getEntity is only called once (it's done both here and in else-case below)
                EntrypointsResponse response = clientResponse.getEntity(EntrypointsResponse.class);

                if (clientResponse.getStatus() == HttpStatus.SC_OK && response.getCode() == null) {
                    // Only save valid entrypointsResponse's to memory
                    entrypointsResponse = response;

                    log.info("KeepAlive() will return true");
                    return true;
                }

                int linksSize = response.getLinksMap() != null ? response.getLinksMap().size() : 0;

                errorMessage = MoreObjects.toStringHelper(response)
                        .add("authToken", response.getAuthToken())
                        .add("code", response.getCode())
                        .add("links", linksSize)
                        .add("details", response.getDetail())
                        .add("message", response.getMessage())
                        .add("first error message", response.getFirstErrorMessage().orElse(null))
                        .add("desc", response.getDesc())
                        .toString();
            } else {
                // important that getEntity is only called once (it's done both here and in if-case above)
                MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(clientResponse)
                        .add("status", clientResponse.getStatus())
                        .add("type", clientResponse.getType());

                if (clientResponse.hasEntity()) {
                    helper.add("entity", clientResponse.getEntity(String.class));
                }

                errorMessage = helper.toString();
            }
        }

        log.info(String.format("KeepAlive() will return false due to: %s", errorMessage));

        return false;
    }

    private EntrypointsResponse authenticatePassword() throws LoginException, AuthorizationException {
        if (request.getCredentials().getSensitivePayload().isEmpty()) {
            return activate(false);
        } else {
            return login(false);
        }
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception {

        switch (transfer.getType()) {
        case BANK_TRANSFER:
            executeBankTransfer(transfer);
            break;
        case PAYMENT:
            executePayment(transfer);
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }

        apiClient.invalidateResponseCache();
    }

    @Override
    public void update(Transfer transfer) throws Exception {
        switch (transfer.getType()) {
        case EINVOICE:
            approveEInvoice(transfer);
            break;
        case PAYMENT:
            updatePayment(transfer);
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }

        apiClient.invalidateResponseCache();
    }

    private void updatePayment(Transfer transfer) throws Exception {
        Transfer originalTransfer = SHBUtils.getOriginalTransfer(transfer);

        SwedishIdentifier transferIdentifier = (SwedishIdentifier) transfer.getSource();
        AccountIdentifier internalTransferIdentifier = new SwedishSHBInternalIdentifier(
                transferIdentifier.getAccountNumber());
        transfer.setSource(internalTransferIdentifier);

        if (Objects.equal(originalTransfer.getHash(), transfer.getHash())) {
            throwTransferError(context.getCatalog().getString("No update needed, payment is not changed."));
        }

        // Find payment to update at bank.

        Optional<List<TransactionListResponse>> upcomingTransactionsResponse = fetchPendingTransactions(entrypointsResponse
                .getLinksMap());

        if (!upcomingTransactionsResponse.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Cannot find payment in list from bank.")
                    .setEndUserMessage(context.getCatalog().getString("Something went wrong."))
                    .build();
        }

        Optional<PaymentEntity> payment = Optional.empty();

        groupLoop : for (TransactionListResponse upcomingTransactionGroup : upcomingTransactionsResponse.get()) {
            for (TransactionEntity upcomingTransaction : upcomingTransactionGroup.getTransactions()) {
                Optional<PaymentEntity> existingPayment = fetchPaymentDetails(upcomingTransaction);

                if (!existingPayment.isPresent()) {
                    continue;
                }

                Transfer existingPaymentTransfer = SHBUtils.PAYMENT_ENTITY_TO_TRANSFER.apply(existingPayment.get());

                if (Objects.equal(existingPaymentTransfer.getHash(), originalTransfer.getHash())) {
                    payment = existingPayment;
                    break groupLoop;
                }
            }
        }

        if (!payment.isPresent()) {
            throwTransferError(context.getCatalog().getString("Cannot find payment in list from bank."));
        }

        if (!SHBUtils.CHANGABLE_PAYMENTS.apply(payment.get())) {
            throwTransferError(context.getCatalog().getString("The payment cannot be updated."));
        }

        // Make change

        PaymentEntity paymentEntity = updatePaymentDetails(transfer, originalTransfer, payment.get());

        LinkEntity signLink = findLinkEntity(paymentEntity.getLinksMap(), REL_TRANSFER_SIGN);

        if (signLink == null) {
            throwTransferError(context.getCatalog().getString("Payment cannot be signed on bank."));
        }

        signPayment(signLink);
    }

    private void approveEInvoice(Transfer transfer) throws Exception {

        Optional<String> approvalId = transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);

        if (!approvalId.isPresent() || Strings.isNullOrEmpty(approvalId.get())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("E-invoice corrupt, no approvalId in payload")
                    .setEndUserMessage(context.getCatalog().getString("Something went wrong."))
                    .build();
        }

        LinkEntity einvoicesLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_PENDING_EINVOICES);
        ClientResponse response = createClientRequest(einvoicesLink.getHref()).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Couldn't fetch e-invoice list from bank")
                    .setEndUserMessage(context.getCatalog().getString("Cannot find e-invoice in list from bank"))
                    .build();
        }

        PendingEInvoicesResponse eInvoicesResponse = response.getEntity(PendingEInvoicesResponse.class);

        List<EInvoicesGroupedEntity> groupedEntityList = eInvoicesResponse.geteInvoicesGrouped();

        Optional<PaymentEntity> einvoice = SHBUtils.findEInvoice(approvalId.get(),
                groupedEntityList.stream()
                        .map(EInvoicesGroupedEntity::geteInvoices)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));

        if (!einvoice.isPresent()) {
            throwTransferError(context.getCatalog().getString("Cannot find e-invoice in list from bank"));
        }

        PaymentEntity paymentEntity = einvoice.get();
        Transfer originalTransfer = SHBUtils.getOriginalTransfer(transfer);

        SwedishIdentifier transferIdentifier = (SwedishIdentifier) transfer.getSource();
        AccountIdentifier internalTransferIdentifier = new SwedishSHBInternalIdentifier(
                transferIdentifier.getAccountNumber());
        transfer.setSource(internalTransferIdentifier);

        if (!Objects.equal(originalTransfer.getHash(), transfer.getHash())) {

            LinkEntity detailsLink = findLinkEntity(paymentEntity.getLinksMap(), REL_EINVOICE_DETAILS);
            PaymentEntity detailedEInvoice = createClientRequest(detailsLink.getHref()).get(PaymentEntity.class);

            paymentEntity = updatePaymentDetails(transfer, originalTransfer, detailedEInvoice);
        }

        signEInvoice(eInvoicesResponse.getLinksMap(), paymentEntity);
    }

    private PaymentEntity updatePaymentDetails(Transfer transfer, Transfer originalTransfer, PaymentEntity eInvoice)
            throws Exception {

        Catalog catalog = context.getCatalog();

        SHBUtils.validateUpdateIsPermitted(catalog, transfer, originalTransfer, eInvoice);

        LinkEntity updateLink = findLinkEntity(eInvoice.getLinksMap(), REL_UDPATE);

        if (updateLink == null) {
            throwTransferError(catalog.getString("Not able to update this e-invoice"));
        }

        PaymentContextResponse paymentContext = (eInvoice.getContext() == null) ?
                getPaymentContext(eInvoice, catalog) : eInvoice.getContext();

        Optional<AccountEntity> source = SHBUtils.findTransferSource(transfer, paymentContext.getFromAccounts());

        if (!source.isPresent()) {
            throwTransferError(catalog.getString("Source account not available"));
        }

        UpdatePaymentRequest request = UpdatePaymentRequest.create(transfer, source.get());

        String url = updateLink.getHref() + "&_method=PUT";
        ClientResponse response = createClientRequest(url).type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, request);
        PaymentEntity updatedEntity = response.getEntity(PaymentEntity.class);

        if (response.getStatus() != HttpStatus.SC_OK ||
                (transfer.getType() == TransferType.EINVOICE && updatedEntity.getApprovalId() == null)) {
            throwTransferError(catalog.getString("Not able to update this e-invoice"));
        }

        return updatedEntity;
    }

    private PaymentContextResponse getPaymentContext(PaymentEntity eInvoice, Catalog catalog) {
        LinkEntity paymentContextLink = findLinkEntity(eInvoice.getLinksMap(), REL_PAYMENT_CONTEXT);
        ClientResponse paymentContextResponse = createClientRequest(paymentContextLink.getHref())
                .get(ClientResponse.class);

        if (paymentContextResponse.getStatus() != HttpStatus.SC_OK) {
            throwTransferError(catalog.getString("Could not fetch payment accounts."));
        }

        return paymentContextResponse.getEntity(PaymentContextResponse.class);
    }

    private void signEInvoice(Map<String, LinkEntity> links, PaymentEntity eInvoice)
            throws Exception {

        Catalog catalog = context.getCatalog();

        LinkEntity approvalLink = findLinkEntity(links, REL_EINVOICE_APPROVAL);

        if (approvalLink == null) {
            throwTransferError(catalog.getString("E-invoice cannot be signed on bank"));
        }

        ApproveEInvoiceRequest request = ApproveEInvoiceRequest.create(Lists.newArrayList(eInvoice.getApprovalId()));

        ClientResponse response = createClientRequest(approvalLink.getHref()).type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, request);

        ApproveEInvoicesResponse approveEInvoicesResponse = response.getEntity(ApproveEInvoicesResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throwTransferError(approveEInvoicesResponse);
        }

        LinkEntity signLink = findLinkEntity(approveEInvoicesResponse.getLinksMap(), REL_EINVOICE_SIGNATURE);

        if (signLink == null) {
            throwTransferError(catalog.getString("Something went wrong when signing e-invoice"));
        }

        response = createClientRequest(signLink.getHref()).type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, new AbstractRequest());

        SignEInvoicesResponse signEInvoicesResponse = response.getEntity(SignEInvoicesResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK || signEInvoicesResponse.getFirstErrorMessage().isPresent()) {
            throwTransferError(signEInvoicesResponse);
        }
    }

    private void executePayment(Transfer transfer) throws Exception {
        Optional<PaymentContextResponse> contextResponse = getPaymentAccounts();

        if (!contextResponse.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED).build();
        }

        AccountEntity source = validateSourceAccount(transfer, contextResponse.get().getFromAccounts());
        Optional<RecipientAccountEntity> destination = GeneralUtils.find(transfer.getDestination(),
                contextResponse.get().getRecipients());

        if (!destination.isPresent()) {
            RecipientAccountEntity recipient = getUnknownRecipient(contextResponse.get(), transfer.getDestination());
            destination = Optional.of(recipient);
        }

        validateMessageForDestinationOrThrow(destination.get(), transfer.getDestinationMessage());

        pay(transfer, contextResponse.get(), source, destination.get());
    }

    private void validateMessageForDestinationOrThrow(RecipientAccountEntity destination, String destinationMessage) {
        OcrValidationConfiguration configuration = destination.getOcrCheck().getValidationConfiguration();
        GiroMessageValidator validator = GiroMessageValidator.create(configuration);

        GiroMessageValidator.ValidationResult validationResult = validator.validate(destinationMessage);

        switch (validationResult.getAllowedType()) {
        case OCR:
            if (!validationResult.getValidOcr().isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setMessage("Invalid OCR for payment recipient requiring OCR")
                        .setEndUserMessage(
                                context.getCatalog().getString(TransferExecutionException.EndUserMessage.INVALID_OCR))
                        .build();
            }
            break;
        case MESSAGE:
            if (!validationResult.getValidMessage().isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setMessage("Invalid message for payment recipient")
                        .setEndUserMessage(context.getCatalog()
                                .getString(TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE))
                        .build();
            }
            break;
        default:
            if (!validationResult.getValidMessage().isPresent() && !validationResult.getValidOcr().isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setMessage("Invalid message and OCR for payment recipient")
                        .setEndUserMessage(context.getCatalog()
                                .getString(TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE))
                        .build();
            }
        }
    }

    private void executeBankTransfer(final Transfer transfer)
            throws Exception {

        if (transfer.getAmount().getValue() < 1) {
            throwTransferError(context.getCatalog().getString("Transfer amount is too small"));
        }

        TransferContextResponse contextResponse = getTransferAccounts();

        AccountEntity source = validateSourceAccount(transfer, contextResponse.getFromAccounts());
        Optional<AccountEntity> destination = SHBUtils.findTransferDestination(transfer, contextResponse);

        if (destination.isPresent()) {
            bankTransferToKnown(transfer, contextResponse, source, destination.get());
        } else {
            bankTransferToOther(transfer, contextResponse, source);
        }
    }

    private void pay(Transfer transfer, PaymentContextResponse contextResponse, AccountEntity source,
            RecipientAccountEntity destination) throws Exception {

        LinkEntity transferCreateLink = findLinkEntity(contextResponse.getLinksMap(), REL_TRANSFER_CREATE);

        PaymentRequest paymentRequest = PaymentRequest.create(transfer, source, destination);

        createPayment(transferCreateLink, paymentRequest);
    }

    private void bankTransferToKnown(final Transfer transfer, TransferContextResponse contextResponse,
            AccountEntity sourceEntity, AccountEntity destinationEntity) throws Exception {
        boolean isBetweenUserAccounts = SHBUtils.isTransferToOwnAccount(transfer, contextResponse);
        TransferMessageFormatter.Messages formattedTransferMessages = transferMessageFormatter
                .getMessages(transfer, isBetweenUserAccounts);

        LinkEntity transferCreate = findLinkEntity(contextResponse.getLinksMap(), REL_TRANSFER_CREATE);
        TransferRequest transferRequest = TransferRequest
                .create(sourceEntity, destinationEntity, transfer.getAmount(), formattedTransferMessages);

        signBankTransfer(transferCreate, transferRequest);
    }

    private RecipientAccountEntity getUnknownRecipient(PaymentContextResponse contextResponse,
            AccountIdentifier destination)
            throws Exception {

        LinkEntity lookupRecipientLink = findLinkEntity(contextResponse.getLinksMap(), REL_LOOKUP_RECIPIENT);

        String recipientUrl = lookupRecipientLink.getHref().replace("{bgPgNumber}",
                destination.getIdentifier(new DisplayAccountIdentifierFormatter()));

        RecipientAccountEntity recipientResponse = createClientRequest(recipientUrl).type(
                MediaType.APPLICATION_JSON).get(RecipientAccountEntity.class);

        Optional<String> firstErrorMessage = recipientResponse.getFirstErrorMessage();
        if (firstErrorMessage.isPresent()) {
            throw TransferExecutionException
                    .builder(SHBUtils.getSignableOperationStatusForErrorCode(recipientResponse.getCode()))
                    .setEndUserMessage(context.getCatalog().getString(firstErrorMessage.get()))
                    .build();
        }

        return recipientResponse;
    }

    private void bankTransferToOther(final Transfer transfer, TransferContextResponse contextResponse,
            AccountEntity sourceEntity) throws Exception {
        TransferMessageFormatter.Messages formattedTransferMessages =
                transferMessageFormatter.getMessages(transfer, false);

        LinkEntity validateRecipient = findLinkEntity(contextResponse.getLinksMap(), REL_TRANSFER_RECIPIENT);
        ValidateRecipientRequest recipientRequest = ValidateRecipientRequest.create(transfer.getDestination());

        ValidRecipientEntity validRecipientEntity = createClientRequest(validateRecipient.getHref())
                .type(MediaType.APPLICATION_JSON).post(ValidRecipientEntity.class, recipientRequest);

        LinkEntity transferCreate = findLinkEntity(validRecipientEntity.getLinksMap(), REL_TRANSFER_CREATE);
        TransferRequest transferRequest = TransferRequest
                .create(sourceEntity, validRecipientEntity, transfer.getAmount(), formattedTransferMessages);

        signBankTransfer(transferCreate, transferRequest);
    }

    private void signBankTransfer(LinkEntity createLink, TransferRequest transferRequest) throws Exception {
        if (createLink == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(context.getCatalog().getString("Destination account is not valid"))
                    .build();
        }

        ClientResponse createResponse = createClientRequest(createLink.getHref())
                .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, transferRequest);

        TransferResponseEntity transferCreateResponse = createResponse.getEntity(TransferResponseEntity.class);

        LinkEntity signLink = findLinkEntity(transferCreateResponse.getLinksMap(), REL_TRANSFER_SIGN);

        if (signLink == null) {
            throwTransferError(transferCreateResponse);
        }

        TransferResponseEntity transferSignResponse = createClientRequest(signLink.getHref())
                .post(TransferResponseEntity.class);

        if (!isOkResponse(transferSignResponse) || transferSignResponse.getFirstErrorMessage().isPresent()) {
            throwTransferError(transferSignResponse);
        }
    }

    private void createPayment(LinkEntity link, PaymentRequest paymentRequest) throws Exception {
        if (link == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(context.getCatalog().getString("Destination account is not valid"))
                    .build();
        }

        ClientResponse response = createClientRequest(link.getHref())
                .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, paymentRequest);

        AbstractResponse createResponse = response.getEntity(AbstractResponse.class);

        LinkEntity signLink = findLinkEntity(createResponse.getLinksMap(), REL_TRANSFER_SIGN);

        if (signLink == null) {
            throwTransferError(createResponse);
        }

        signPayment(signLink);
    }

    private void signPayment(LinkEntity link) throws Exception {
        ClientResponse response = createClientRequest(link.getHref()).type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        AbstractResponse signResponse = response.getEntity(AbstractResponse.class);
        if (response.getStatus() != HttpStatus.SC_OK || signResponse.getFirstErrorMessage().isPresent()) {
            throwTransferError(signResponse);
        }
    }

    private boolean isOkResponse(TransferResponseEntity transferSignResponse) {
        return transferSignResponse.getStatus() == null || Objects.equal(transferSignResponse.getStatus(), "APPROVED");
    }

    private void throwTransferError(TransferResponseEntity transferCreateResponse) {
        if (!transferCreateResponse.getFirstErrorMessage().isPresent()) {
            throwTransferError(transferCreateResponse.getStatus() != null ? transferCreateResponse.getStatus() : "");
        }

        throwTransferError((AbstractResponse) transferCreateResponse);
    }

    private void throwTransferError(AbstractResponse response) {
        String errorMessage = null;
        if (response.getFirstErrorMessage().isPresent()) {
            errorMessage = response.getFirstErrorMessage().get();
        }

        Optional<String> firstErrorMessage = response.getFirstErrorMessage();
        if (firstErrorMessage.isPresent()) {
            errorMessage = firstErrorMessage.get();
        }

        if (errorMessage != null) {
            errorMessage = errorMessage.replaceAll("\n", "");
        } else {
            errorMessage = context.getCatalog().getString("Something went wrong.");
        }

        SignableOperationStatuses status = SHBUtils.getSignableOperationStatusForErrorCode(response.getCode());

        throwTransferError(errorMessage, status);
    }

    private void throwTransferError(String userMessage) {
        throwTransferError(userMessage, SignableOperationStatuses.FAILED);
    }

    private void throwTransferError(String userMessage, SignableOperationStatuses status) {
        throw TransferExecutionException.builder(status)
                .setEndUserMessage(userMessage).setMessage(userMessage).build();
    }

    private AccountEntity validateSourceAccount(Transfer transfer, AccountListResponse accountListResponse) {
        Optional<AccountEntity> source = SHBUtils.findTransferSource(transfer, accountListResponse);
        if (!source.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(context.getCatalog().getString("Source account not found")).build();
        }
        return source.get();
    }

    private void updateTransactionsAndAccounts(final CardEntity cardEntity) throws Exception {
        LinkEntity cardTransactionLink = findLinkEntity(cardEntity.getLinksMap(), "card-transactions");

        CardTransactionListResponse cardTransactionListResponse = apiClient.fetchCardTransactionListResponse(
                cardTransactionLink);

        Optional<Account> account = constructAccount(cardEntity);

        // Don't include inactive cards which don't have a card number.
        if (!account.isPresent()) {
            return;
        }

        List<Transaction> transactions = Lists.newArrayList(Iterables.transform(
                cardTransactionListResponse.getTransactions(), te -> te.toTransaction(cardEntity)));

        context.updateTransactions(account.get(), transactions);
    }

    private Optional<List<TransactionListResponse>> fetchPendingTransactions(Map<String,LinkEntity> links) throws Exception {
        LinkEntity transactionsPendingLink = findLinkEntityNewApi(links, REL_PENDING_PAYMENTS, REL_PENDING_PAYMENTS_V3);

        ClientResponse response = createClientRequest(
                transactionsPendingLink.getHref()).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            log.error("Couldn't fetch pending transaction list from bank");
            return Optional.empty();
        }

        PendingTransactionsResponse upcomingTransactionsResponse = response.getEntity(PendingTransactionsResponse.class);

        if (upcomingTransactionsResponse.getTransactionGroups() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(upcomingTransactionsResponse.getTransactionGroups());
    }

    private Account updateTransactionsAndAccounts(AccountEntity accountEntity, ImmutableMap<String, CardEntity> cards,
            ImmutableMap<String, TransactionListResponse> upcomingTransactionsByNumber) throws Exception {

        // Fetch the account and transactions.

        LinkEntity transactionLink = findLinkEntity(accountEntity.getLinksMap(), "transactions");

        TransactionListResponse transactionListResponse = apiClient.fetchTransactionListResponse(transactionLink);

        final Account account = constructAccount(accountEntity);

        // Construct and add the regular (non-credit-card) transactions.

        Date today = DateUtils.flattenTime(new Date());
        List<TransactionEntity> transactionEntities = transactionListResponse.getTransactions();
        List<Transaction> transactions = Lists.newArrayList();

        transactions.addAll(Lists.newArrayList(Iterables.transform(transactionEntities,
                SHBUtils.TRANSACTION_ENTITY_TO_TRANSACTION)));

        ImmutableSet<String> amountsOfTodaysTransactions = FluentIterable.from(transactionEntities)
                .filter(SHBUtils.getTransactionsDoneOnDateFilter(ThreadSafeDateFormat.FORMATTER_DAILY.format(today)))
                .transform(SHBUtils.TRANSACTION_TO_POSITIVE_AMOUNT)
                .toSet();

        // Fetch and add the credit-card transactions.
        LinkEntity cardTransactionListLink = findLinkEntity(accountEntity.getLinksMap(), "card-transactions");

        if (cardTransactionListLink != null) {
            String cardTransactionListUrl = cardTransactionListLink.getHref();

            final CardEntity cardEntity = cards.get(SHBUtils.stripQueryString(cardTransactionListUrl));

            CardTransactionListResponse cardTransactionListResponse = apiClient.fetchCardTransactionListResponse(
                    cardTransactionListLink);

            if (cardTransactionListResponse.getTransactions() != null) {
                transactions.addAll(Lists.newArrayList(Iterables.transform(
                        cardTransactionListResponse.getTransactions(),
                        te -> te.toTransaction(cardEntity))));
            } else {
                log.warn(String.format("Credit card transaction list NULL from SHB. "
                                + "Proceeding with rest of the refresh. Failed with message: %s",
                                cardTransactionListResponse.getMessage()));
            }

        }

        // Add upcoming transactions ("kommande betalningar")
        if (upcomingTransactionsByNumber.containsKey(accountEntity.getNumber())) {

            TransactionListResponse transactionGroup = upcomingTransactionsByNumber.get(accountEntity.getNumber());

            // Due to the possibility to make upcoming payments where the amount is more than the current amount
            // of the account we have to check that the transactions are PENDING. Other alternatives are SUSPENDED
            // or ABANDONED, which means that the transactions has been stopped at the bank.
            FluentIterable<TransactionEntity> filteredTransactions = FluentIterable.from(
                    transactionGroup.getTransactions()).filter(SHBUtils.REMOVE_ABANDONED_OR_SUSPENDED_TRANSACTIONS);

            for (TransactionEntity entity : filteredTransactions) {

                Date dueDate = DateUtils.flattenTime(DateUtils.parseDate(entity.getDueDate()));

                if (dueDate.getTime() == today.getTime()) {

                    String cleanAmount = SHBUtils.AMOUNT_TO_POSITIVE_AMOUNT.apply(entity.getAmount());
                    if (amountsOfTodaysTransactions.contains(cleanAmount)) {
                        // If we have an upcoming transaction with dueDate of today
                        // We check if it's included in incoming "normal" transactions matching on date and amount
                        // Unfortunately descriptions are not the same
                        continue;
                    }
                }

                Transaction transaction = SHBUtils.TRANSACTION_ENTITY_TO_UPCOMING_TRANSACTION.apply(entity);

                Optional<PaymentEntity> payment = fetchPaymentDetails(entity);
                if (payment.isPresent() && SHBUtils.CHANGABLE_PAYMENTS.apply(payment.get())) {

                    Transfer transfer = SHBUtils.PAYMENT_ENTITY_TO_TRANSFER.apply(payment.get());
                    transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

                    transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                            MAPPER.writeValueAsString(transfer));
                    transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                            UUIDUtils.toTinkUUID(transfer.getId()));
                }

                transactions.add(transaction);
            }
        }

        // Filter out any internal transfers for credit card accounts.

        transactions = Lists.newArrayList(Iterables.filter(transactions, transaction -> {
            if (account.getType() == AccountTypes.CREDIT_CARD
                    && transaction.getType() != TransactionTypes.CREDIT_CARD
                    && Objects.equal(transaction.getDescription().toLowerCase(), "periodens köp")) {
                return false;
            }

            return true;
        }));

        // Finish updating the account.

        return context.updateTransactions(account, transactions);
    }

    private Optional<PaymentEntity> fetchPaymentDetails(TransactionEntity transaction) throws Exception {
        Optional<LinkEntity> detailsLink = SHBUtils.findLinkEntity(transaction.getLinksMap(), REL_PAYMENT_DETAILS);
        if (detailsLink.isPresent()) {
            ClientResponse response = createClientRequest(detailsLink.get().getHref()).get(ClientResponse.class);
            if (response.getStatus() == HttpStatus.SC_OK) {
                return Optional.ofNullable(response.getEntity(PaymentEntity.class));
            }
        }
        return Optional.empty();
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(List<Account> updatedAccounts) throws Exception {
        TransferContextResponse response = getTransferAccounts();

        AccountGroupEntity ownSourceGroup = SHBUtils.findAccountGroup(ACCOUNT_GROUP_TYPE_OWN,
                response.getFromAccounts().getAccountGroups());

        if (ownSourceGroup == null) {
            return Collections.emptyMap();
        }

        List<AccountEntity> destinationAccounts = Lists.newArrayList();
        for (AccountGroupEntity group : response.getToAccounts().getAccountGroups()) {
            destinationAccounts.addAll(group.getAccounts());
        }

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(ownSourceGroup.getAccounts())
                .setTinkAccounts(updatedAccounts)
                .setDestinationAccounts(destinationAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SE_SHB_INTERNAL, SwedishSHBInternalIdentifier.class)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(List<Account> updatedAccounts)
            throws Exception {
        Optional<PaymentContextResponse> response = getPaymentAccounts();

        if (!response.isPresent()) {
            return Collections.emptyMap();
        }

        AccountGroupEntity ownSourceGroup = SHBUtils.findAccountGroup(ACCOUNT_GROUP_TYPE_OWN,
                response.get().getFromAccounts().getAccountGroups());

        if (ownSourceGroup == null) {
            return Collections.emptyMap();
        }

        List<RecipientAccountEntity> destinationAccounts = Lists.newArrayList();
        destinationAccounts.addAll(response.get().getRecipients());

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(ownSourceGroup.getAccounts())
                .setTinkAccounts(updatedAccounts)
                .setDestinationAccounts(destinationAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .matchDestinationAccountsOn(AccountIdentifier.Type.SE_SHB_INTERNAL, SwedishSHBInternalIdentifier.class)
                .build();
    }

    /**
     * Helper method to get the API entrypoints.
     */
    private EntrypointsResponse getEntryPoints() {
        return createClientRequest("https://m.handelsbanken.se/open/entrypoint/priv").get(
                EntrypointsResponse.class);
    }

    private Optional<PaymentContextResponse> getPaymentAccounts() throws Exception {
        LinkEntity transferContextLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_PAYMENT_CONTEXT);
        ClientResponse response = createClientRequest(transferContextLink.getHref())
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            FailedResponse failedResponse = response.getEntity(FailedResponse.class);

            // If the user is younger than 16, payments are not allowed.
            if (Objects.equal(failedResponse.getCode(), "10573")) {
                return Optional.empty();
            }

            log.error("Could not fetch payment accounts. Error: " + failedResponse.getDetail() + ". Code: "
                    + failedResponse.getCode());
            return Optional.empty();
        }
        return Optional.of(response.getEntity(PaymentContextResponse.class));
    }

    private TransferContextResponse getTransferAccounts() throws Exception {
        LinkEntity transferContextLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_BANK_TRANSFER_CONTEXT);
        return createClientRequest(transferContextLink.getHref()).get(TransferContextResponse.class);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        EntrypointsResponse entrypointsResponse;

        switch (request.getCredentials().getType()) {

        case PASSWORD:
            entrypointsResponse = authenticatePassword();
            break;
        case MOBILE_BANKID:
            entrypointsResponse = authenticateBankId();
            break;
        default:
            throw new IllegalArgumentException("Unsupported credentials type: " + request.getCredentials().getType());
        }

        Preconditions.checkNotNull(entrypointsResponse);
        Preconditions.checkNotNull(entrypointsResponse.getAuthToken(),
                MoreObjects.toStringHelper(entrypointsResponse)
                        .add("authToken", entrypointsResponse.getAuthToken())
                        .add("code", entrypointsResponse.getCode())
                        .add("links", entrypointsResponse.getLinksMap().size())
                        .add("details", entrypointsResponse.getDetail())
                        .add("message", entrypointsResponse.getMessage())
                        .add("first error message", entrypointsResponse.getFirstErrorMessage().orElse(null))
                        .add("desc", entrypointsResponse.getDesc())
                        .toString());

        this.entrypointsResponse = entrypointsResponse;
        return true;
    }

    /**
     * Login using the PIN-code and a created profile (consisting of a profile ID and a profile private key).
     */
    private EntrypointsResponse login(boolean isSecondTry) throws LoginException, AuthorizationException {
        Credentials credentials = request.getCredentials();
        LibTFA tfa = new LibTFA(credentials.getField(Field.Key.USERNAME), credentials.getId());

        // Load the private key and profile id from the sensitive payload.
        tfa.loadPayload(credentials.getSensitivePayload());

        // Get entrypoints.

        EntrypointsResponse entrypoints = getEntryPoints();

        // Handshake.

        String pinnedLoginUrl = findLinkEntity(entrypoints.getLinksMap(), "pinned-login").getHref();

        HandshakeResponse handshakeResponse = createClientRequest(pinnedLoginUrl).type(
                MediaType.APPLICATION_JSON).post(HandshakeResponse.class, tfa.createHandshakeRequest());

        tfa.handleHandshakeResponse(handshakeResponse);

        // Get server profile.

        String serverProfileUrl = findLinkEntity(handshakeResponse.getLinksMap(), "getServerProfile").getHref();

        ServerProfileResponse serverProfileResponse = createClientRequest(serverProfileUrl).type(
                MediaType.APPLICATION_JSON).post(ServerProfileResponse.class,
                tfa.createServerProfileRequest(credentials.getPassword()));

        if (Objects.equal("107", serverProfileResponse.getCode())) {

            // TODO: Replace with new AgentWorker
            if ((request.isUpdate() || request.isCreate()) && !isSecondTry) {
                // if ((request instanceof UpdateCredentialsRequest || request instanceof CreateCredentialsRequest) &&
                // !isSecondTry) {
                return activate(true);
            } else {
                if (serverProfileResponse.getMessage().toLowerCase().contains("appen är inte längre aktiv")) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.BLOCKED_DUE_TO_INACTIVITY.getKey());
                }
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring- SHB - Login failed (serverProfileResponse) with message %s, code %s, error message %s",
                                serverProfileResponse.getMessage(),
                                serverProfileResponse.getCode(),
                                serverProfileResponse.getFirstErrorMessage()));
            }
        }

        // if (!Objects.equal(payload.get("pdeviceServerProfile"), serverProfileResponse.getPdeviceServerProfile())) {
        // context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR,
        // "Felaktig personlig kod, vänligen försök igen.");
        // return null;
        // }

        // Challenge/response.

        String getChallengeUrl = findLinkEntity(serverProfileResponse.getLinksMap(), "getChallenge").getHref();

        CallengeResponse challengeResponse = createClientRequest(getChallengeUrl).type(
                MediaType.APPLICATION_JSON).post(CallengeResponse.class, tfa.createChallengeRequest());

        if (!Strings.isNullOrEmpty(challengeResponse.getCode())) {
            switch(challengeResponse.getCode()) {
            case "100":
                LocalDate dateOfLoginChange = new LocalDate(2017, 6, 20);
                Date credentialUpdatedDate = credentials.getUpdated();
                if (credentialUpdatedDate == null || !credentialUpdatedDate.after(dateOfLoginChange.toDate())) {
                    // 100 - "Temporary error" -> Which means, in this state, that the `Device Security Context Id` was wrong.
                    // The user must re-activate their credential.
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
                // fall through
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring- SHB - Login failed (ChallengeResponse) with message %s, code %s, error message %s",
                                challengeResponse.getMessage(),
                                challengeResponse.getCode(),
                                challengeResponse.getFirstErrorMessage()));
            }
        }

        tfa.handleChallengeResponse(challengeResponse);

        // Validate signature.

        String validateSignatureUrl = findLinkEntity(challengeResponse.getLinksMap(), "validateSignature").getHref();

        ValidateSignatureResponse validateSignatureResponse = createClientRequest(validateSignatureUrl).type(
                MediaType.APPLICATION_JSON).post(ValidateSignatureResponse.class,
                tfa.createValidateSignatureRequest());

        if (validateSignatureResponse.getCode() != null) {
            // Code is only present if an error occurred
            switch(validateSignatureResponse.getCode()) {
            case "100":
                LocalDate dateOfLoginChange = new LocalDate(2017, 6, 20);
                Date credentialUpdatedDate = credentials.getUpdated();
                if (credentialUpdatedDate == null || !credentialUpdatedDate.after(dateOfLoginChange.toDate())) {
                    // 100 - "Temporary error" -> Which means, in this state, that the `Device Security Context Id` was wrong.
                    // The user must re-activate their credential.
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
                // fall through
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring- SHB - Login failed (validateSignatureResponse) with message %s, code %s, error message %s",
                                challengeResponse.getMessage(),
                                challengeResponse.getCode(),
                                challengeResponse.getFirstErrorMessage()));
            }
        }

        if (!validateSignatureResponse.getResult().equals("AUTHENTICATED")) {
            log.warn(String.format("#login-refactoring - SHB - Login failed (validateSignatureResponse/result) with message %s, code %s, error message %s, result %s",
                            validateSignatureResponse.getMessage(),
                            validateSignatureResponse.getCode(),
                            validateSignatureResponse.getFirstErrorMessage(),
                            validateSignatureResponse.getResult()));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // Authorize.

        String authorizeUrl = findLinkEntity(validateSignatureResponse.getLinksMap(), "authorize").getHref();

        AuthorizeResponse authorizeResponse = createClientRequest(authorizeUrl)
                .type(MediaType.APPLICATION_JSON).post(AuthorizeResponse.class, tfa.createAuthorizeRequest());

        // Get authorization token.

        String appEntrypointUrl = findLinkEntity(authorizeResponse.getLinksMap(), "application-entry-point").getHref();

        // Return the authorization token.
        return createClientRequest(appEntrypointUrl).type(MediaType.APPLICATION_JSON)
                .get(EntrypointsResponse.class);
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return isAlive(this.session);
    }

    @Override
    public boolean keepAlive() throws Exception {
        return isAlive(this.session);
    }

    @Override
    public void persistLoginSession() {

        if (session == null) {
            return;
        }

        session.setCookiesFromClient(client);
        credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {

        session = credentials.getPersistentSession(Session.class);

        if (session == null) {
            return;
        }

        addSessionCookiesToClient(client, session);
    }

    /** Handelsbankens authentication logic requires that the Session belongs to the IP-address
     *  that created the Session.
     *
     *  Since we're using multiple AWS machines, the IP-address could have changed
     *  if the machine assigned to the user are updating or crashed.
     *  In that case the user will be assigned to a different machine with a new IP-address
     *  for a short period of time, which will force the user to login again.
     *
     *  However, we don't want to clear the Session if the request wasn't initiated by the user,
     *  instead we wan't to keep the Session that belongs to the unavailable machine, in order to
     *  keep the connection alive at the original machine when it's up and running again.
     *
     *  If the user initiated the request, we want to create a new Session on the temporary machine
     *  and then create a new Session again when the the original machine is up and running again.
     */
    @Override
    public void clearLoginSession() {
        if (request.getType().equals(CredentialsRequestType.KEEP_ALIVE)) {
            log.info("Don't clear the Session on failed KEEP_ALIVE requests");
            return;
        }

        // Clean the session in memory
        session = null;
        // Remove the entrypointsResponse
        entrypointsResponse = null;

        // Clean the persisted session
        credentials.removePersistentSession();

        // Reinstantiate the client to remove any Session set on the client
        initClient();
    }

    @Override
    public void logout() throws Exception {
        createClientRequest(BASE_URL + "/bb/glss/aa/global/doLogout/1.0").get(String.class);
    }

    private Map<AccountEntity, Account> getAccounts() {
        if (accountEntityAccountMap != null) {
            return accountEntityAccountMap;
        }

        accountEntityAccountMap = new HashMap<>();

        try {
            List<AccountEntity> accountEntities = fetchAccountEntities(entrypointsResponse);
            for (AccountEntity entity : accountEntities) {
                accountEntityAccountMap.put(entity, constructAccount(entity));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return accountEntityAccountMap;
    }

    private Map<CardEntity, Account> getCreditCards() {
        if (cardEntityAccountMap != null) {
            return cardEntityAccountMap;
        }

        cardEntityAccountMap = new HashMap<>();

        try {
            List<CardEntity> cardEntities = fetchCardEntities(entrypointsResponse);

            for (CardEntity entity : cardEntities) {
                if (shouldExcludeHandledAccount(entity)) {
                    continue;
                }

                Optional<Account> account = constructAccount(entity);
                if (!account.isPresent()) {
                    continue;
                }
                cardEntityAccountMap.put(entity, account.get());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return cardEntityAccountMap;
    }

    private List<LoanEntity> getLoans() {
        if (loans != null) {
            return loans;
        }

        LinkEntity loansLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_LOANS);

        LoansResponse loansResponse = createClientRequest(loansLink.getHref()).get(LoansResponse.class);

        if (loansResponse.getLoans() == null) {
            return Collections.emptyList();
        }

        loans = loansResponse.getLoans();
        return loans;
    }

    private void updateAccountsPerType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> context.cacheAccount(set.getValue()));
    }

    private void updateCreditCards() {
        getCreditCards().forEach((entity, account) -> context.cacheAccount(account));
    }

    private void updateLoans() {
        getLoans().forEach(entity -> {
            try {
                Account account = entity.toAccount();
                Loan loan = entity.toloan();

                context.cacheAccount(account, AccountFeatures.createForLoan(loan));
            } catch (Exception e) {
                log.error("Exception caught when updating loans", e);
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateEinvoices() {
        LinkEntity einvoicesLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_PENDING_EINVOICES);
        ClientResponse response = createClientRequest(einvoicesLink.getHref()).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            log.warn("HTTP Status not 200. Ignoring fetch of einvoices");
            return;
        }

        PendingEInvoicesResponse eInvoicesResponse = response.getEntity(PendingEInvoicesResponse.class);
        List<EInvoicesGroupedEntity> groupedEntityList = eInvoicesResponse.geteInvoicesGrouped();

        List<Transfer> einvoices = Lists.newArrayList(groupedEntityList.stream()
                .map(EInvoicesGroupedEntity::geteInvoices)
                .flatMap(List::stream)
                .map(SHBUtils.PAYMENT_ENTITY_TO_TRANSFER::apply)
                .collect(Collectors.toList()));

        context.updateEinvoices(einvoices);
    }

    private void updateTransferDestinations() {
        TransferDestinationsResponse response = new TransferDestinationsResponse();

        try {
            response.addDestinations(getTransferAccountDestinations(context.getUpdatedAccounts()));
            response.addDestinations(getPaymentAccountDestinations(context.getUpdatedAccounts()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        context.updateTransferDestinationPatterns(response.getDestinations());
    }

    private void refreshTransactionalAccountsAndTransactions() {
        try {
            List<AccountEntity> accountEntities = getAccounts().entrySet().stream().map(Map.Entry::getKey).collect(
                    Collectors.toList());
            List<CardEntity> cardEntities = getCreditCards().entrySet().stream().map(Map.Entry::getKey).collect(
                    Collectors.toList());

            ImmutableMap<String, CardEntity> cardsByUrl = FluentIterable.from(cardEntities)
                    .uniqueIndex(SHBUtils.getEntitiesToStrippedLinkFunction("card-transactions"));

            Optional<List<TransactionListResponse>> upcomingTransactionsResponse = fetchPendingTransactions(
                    entrypointsResponse.getLinksMap());

            ImmutableMap<String, TransactionListResponse> upcomingTransactionsByNumber = ImmutableMap.of();

            if (upcomingTransactionsResponse.isPresent()) {
                upcomingTransactionsByNumber = FluentIterable.from(upcomingTransactionsResponse.get())
                        .filter(SHBUtils.getTransactionListGroupsWithAccountNumber(credentials))
                        .uniqueIndex(SHBUtils.TRANSACTION_LIST_GROUP_TO_ACCOUNT_NUMBER);
            }

            for (AccountEntity accountEntity : accountEntities) {
                updateTransactionsAndAccounts(accountEntity, cardsByUrl, upcomingTransactionsByNumber);
            }

            for (CardEntity cardEntity : cardEntities) {
                if (shouldExcludeHandledAccount(cardEntity)) {
                    continue;
                }
                updateTransactionsAndAccounts(cardEntity);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            updateEinvoices();
            break;

        case TRANSFER_DESTINATIONS:
            updateTransferDestinations();
            break;

        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
            updateAccountsPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
            // All these account types share refresh method since we cannot fetch them separately.
            // However, they are only refreshed once.
            if (hasRefreshedTransactions) {
                break;
            }
            hasRefreshedTransactions = true;
            refreshTransactionalAccountsAndTransactions();
            break;

        case CREDITCARD_ACCOUNTS:
            updateCreditCards();
            break;

        case LOAN_ACCOUNTS:
            updateLoans();
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                updateInvestmentAccounts();
            } catch (Exception e) {
                // Don't fail the whole refresh just because we failed updating investment data but log error.
                log.error("Caught exception while updating investment data", e);
            }
            break;

        case INVESTMENT_TRANSACTIONS:
            // nop
            break;
        }
    }

    private void updateInvestmentAccounts() {
        // This request will give us information about the aggregated holdings of securities.
        // We can then also use the links we get to fetch more detailed information.
        LinkEntity holdingsLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_SECURITIES_HOLDINGS);

        // If the user is younger than 18, fetching the holdings summary will fail with a 400.
        // Thus we expect that this can happen, but other error will be passed out of this method.
        HoldingsSummaryResponse holdingsSummaryResponse;
        try {
            holdingsSummaryResponse = createClientRequest(holdingsLink.getHref()).get(HoldingsSummaryResponse.class);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();
            if (response.getStatus() != 400) {
                throw e;
            }

            AbstractResponse errorResponse = response.getEntity(AbstractResponse.class);
            if (!Objects.equal(errorResponse.getDetail(),
                    "Värdepappersinformation via Mobiltjänsten ej tillgänglig för personer under 18 år")) {
                log.info(String.format("could not fetch holdings summary - status: %s, code: %s, detail: %s",
                        errorResponse.getStatus(), errorResponse.getCode(), errorResponse.getDetail()));
            }
            return;
        }

        if (holdingsSummaryResponse.getCustodyAccounts() == null ||
                holdingsSummaryResponse.getCustodyAccounts().isEmpty()) {
            return;
        }

        holdingsSummaryResponse.getCustodyAccounts().forEach(custodyAccount -> {
            String custodyAccountType = custodyAccount.getType().toLowerCase();
            switch (custodyAccountType) {
            case "fund_summary":
                updateFundHoldings(custodyAccount);
                break;
            case "isk":
                // Intentional fall through
            case "normal":
                updateCustodyAccount(custodyAccount);
                break;
            case "kapital":
                updatePensionAccount(custodyAccount);
                break;
            default:
                log.info(String.format("Not yet implemented custody account - type: %s, relative links: %s",
                                custodyAccountType, custodyAccount.getLinksMap().keySet().stream()
                                        .distinct()
                                        .collect(Collectors.joining(", "))));
            }
        });
    }

    private void updateFundHoldings(CustodyAccountEntity custodyAccount) {
        LinkEntity fundHoldingsLink = findLinkEntity(custodyAccount.getLinksMap(), REL_FUND_HOLDINGS);
        if (fundHoldingsLink == null) {
            return;
        }

        FundHoldingsResponse fundHoldingsResponse = createClientRequest(
                fundHoldingsLink.getHref()).get(FundHoldingsResponse.class);

        if (fundHoldingsResponse.getUserFundHoldings() == null) {
            return;
        }

        Account account = fundHoldingsResponse.createFundSummaryAccount(custodyAccount);
        Portfolio portfolio = fundHoldingsResponse.createFundPortfolio(custodyAccount);

        List<Instrument> instruments = Lists.newArrayList();
        fundHoldingsResponse.getUserFundHoldings().getFundHoldingList().forEach(fundHoldingsEntity ->
            fundHoldingsEntity.toInstrument().ifPresent(instruments::add));
        portfolio.setInstruments(instruments);

        context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
    }

    private void updateCustodyAccount(CustodyAccountEntity custodyAccount) {
        LinkEntity custodyAccountLink = findLinkEntity(custodyAccount.getLinksMap(), REL_CUSTODY_ACCOUNTS);
        if (custodyAccountLink == null) {
            return;
        }

        String custodyAccountResponseString = createClientRequest(
                custodyAccountLink.getHref()).get(String.class);

        try {
            CustodyAccountResponse custodyAccountResponse = MAPPER.readValue(custodyAccountResponseString, CustodyAccountResponse.class);

            Account account = custodyAccountResponse.toAccount();
            Portfolio portfolio = custodyAccountResponse.toPortfolio();

            List<LinkEntity> holdingsDetailsLinks = custodyAccountResponse.getHoldingLists().stream()
                    .map(HoldingsListsEntity::getHoldingList)
                    .flatMap(Collection::stream)
                    .map(CustodyHoldingsEntity::getLinks)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

            List<Instrument> instruments = Lists.newArrayList();
            holdingsDetailsLinks.stream()
                    .filter(l -> Objects.equal(REL_SECURITY_HOLDING, l.getRel().toLowerCase()))
                    .forEach(linkEntity -> {
                        String securityHoldingsString = createClientRequest(linkEntity.getHref()).get(String.class);
                        log.info("#handelsbanken - security holdings response: " + securityHoldingsString);

                        SecurityHoldingsResponse securityHoldingsResponse;
                        try {
                            securityHoldingsResponse = MAPPER.readValue(securityHoldingsString, SecurityHoldingsResponse.class);
                        } catch (IOException e) {
                            log.warn("#handelsbanken - Could not deserialize string", e);
                            return;
                        }

                        securityHoldingsResponse.toInstrument().ifPresent(instruments::add);
                    });
            portfolio.setInstruments(instruments);

            context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        } catch (Exception e) {
            log.warnExtraLong("failed to update custody accounts" + custodyAccountResponseString,
                    LogTag.from("#shb"), e);
        }
    }

    private void updatePensionAccount(CustodyAccountEntity custodyAccount) {
        LinkEntity pensionDetailsLink = findLinkEntity(custodyAccount.getLinksMap(), REL_PENSION_DETAILS);
        if (pensionDetailsLink == null) {
            return;
        }
        try {
            PensionDetailsResponse pensionDetails = createClientRequest(pensionDetailsLink.getHref())
                    .get(PensionDetailsResponse.class);
            Account account = pensionDetails.toAccount(custodyAccount);
            Portfolio portfolio = pensionDetails.toPortfolio(custodyAccount);

            portfolio.setInstruments(
                    pensionDetails.getFunds().stream()
                            .map(fund -> findLinkEntity(fund.getLinksMap(), REL_FUND_HOLDING_DETAILS))
                            .filter(java.util.Objects::nonNull)
                            .map(link -> createClientRequest(link.getHref())
                                    .get(FundAccountHoldingDetailResponse.class))
                            .map(FundAccountHoldingDetailResponse::toInstrument)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList())
            );
            context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
        } catch (Exception e) {
            log.warn(String.format("%s: Could not fetch pension details", LOG_TAG_PENSION_DETAILS), e);
        }
    }

    private List<AccountEntity> fetchAccountEntities(EntrypointsResponse entrypointsResponse) throws Exception {
        LinkEntity accountsLink = findLinkEntity(entrypointsResponse.getLinksMap(), REL_ACCOUNTS);
        AccountListResponse accountListResponse = apiClient.fetchAccountListResponse(accountsLink);

        return accountListResponse.toAccountEntityList();
    }

    private List<CardEntity> fetchCardEntities(EntrypointsResponse entrypointsResponse) throws Exception {

        LinkEntity cardsLink = findLinkEntityNewApi(entrypointsResponse.getLinksMap(), REL_CARDS, REL_CARDS_V2);
        CardListResponse cardListResponse = apiClient.fetchCardListResponse(cardsLink);

        List<AccountEntity> accountEntities = fetchAccountEntities(entrypointsResponse);

        // This is done because SHB has an account which is both a creditcard and a checking account
        // We fetch it through it's AccountEntity and therefore filter away  it's CardEntity
        List<String> cardEntitiesToRemove = FluentIterable.from(accountEntities)
                .filter(SHBUtils.getEntitiesWithLinkPredicate("card-transactions"))
                .transform(SHBUtils.getEntitiesToStrippedLinkFunction("card-transactions"))
                .toList();

        // SHB doesn't only return credit cards here, there filter away all without card-transactions link
        return FluentIterable.from(cardListResponse.toCardEntityList())
                .filter(SHBUtils.getEntitiesWithLinkPredicate("card-transactions"))
                .filter(
                        Predicates.not(
                                SHBUtils.getEntitiesWithLinkPresentInList("card-transactions", cardEntitiesToRemove)))
                .toList();
    }

    /**
     * Creditcard transactions are already added from the account entity if cardEntity.type = A,
     * hence, excluding them here. Log for a while so we don't remove anything unwanted. NOTE we
     * are not sure about the A exclusion, hence logging also now to verify.
     */
    private boolean shouldExcludeHandledAccount(CardEntity entity) {
        if (Objects.equal(entity.getTypeCode(), "A")) {
            log.info(String.format("Not handling SHB account: %s with type %s ", entity.getName(), entity.getTypeCode()));
            return true;
        }
        return false;
    }

    private Account constructAccount(AccountEntity accountEntity) throws Exception {
        LinkEntity transactionLink = findLinkEntity(accountEntity.getLinksMap(), "transactions");

        TransactionListResponse transactionListResponse = apiClient.fetchTransactionListResponse(transactionLink);

        final Account account = transactionListResponse.toAccount();
        account.setName(accountEntity.getName());

        Optional<LinkEntity> transactionsLink = SHBUtils.findLinkEntity(accountEntity.getLinksMap(), "card-transactions");

        if (transactionsLink.isPresent()) {
            account.setType(AccountTypes.CREDIT_CARD);
        } else if (Strings.nullToEmpty(account.getName()).equalsIgnoreCase("Sparkonto")) {
            account.setType(AccountTypes.SAVINGS);
        } else if (Strings.nullToEmpty(account.getName()).equalsIgnoreCase("e-kapitalkonto")) {
            account.setType(AccountTypes.SAVINGS);
        }

        return account;
    }

    private Optional<Account> constructAccount(CardEntity cardEntity) throws Exception {
        LinkEntity cardTransactionListUrl = findLinkEntity(cardEntity.getLinksMap(), "card-transactions");

        CardTransactionListResponse response = apiClient.fetchCardTransactionListResponse(cardTransactionListUrl);

        Account account = response.toAccount(cardEntity);

        // Don't include inactive cards which don't have a card number.

        if (Strings.isNullOrEmpty(account.getBankId())) {
            return Optional.empty();
        }

        // Instead of giving users duplicate accounts we will fail whole refresh here
        Preconditions.checkState(
                account.getBankId().matches(REGEXP_OR_JOINER.join("[0-9]{8,9}", "[0-9]{4}( \\*{4}){2} [0-9]{4}")),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        return Optional.ofNullable(account);
    }

    private enum UserMessage implements LocalizableEnum {
        ACTIVATION_NEEDED(new LocalizableKey("You need to activate the connection with your Handelsbanken card reader.")),
        WRONG_CARD(new LocalizableKey("Are you trying to use your charge card to login? Please use your login card for BankID. The response should be 9 figures.")),
        CODE_ACTIVATION_NEEDED(new LocalizableKey("You need to activate your personal code for telephone and mobile services on the internet bank.")),
        BANKID_ACTIVATION_NEEDED(new LocalizableKey("You need to activate your BankID in the Handelsbanken app.")),
        BLOCKED_DUE_TO_INACTIVITY(new LocalizableKey("Handelsbanken is no longer active for the specified user. This could be due to that the Handelsbanken app for this device has not been active in 90 days. To login again you need to activate the app again.")),
        WRONG_ACTIVATION_CODE(new LocalizableKey("Incorrect answer code. The challenge code is only active for four minutes. Press OK to restart.")),
        INCORRECT_CREDENTIALS(new LocalizableKey("You have used incorrect credentials. Please try again.")),
        PERMISSION_DENIED(new LocalizableKey("You lack the sufficient permissions for this service.")),
        PERM_BLOCKED_CARD(new LocalizableKey("The card you are using is blocked. Please contact Handelsbanken technical support or visit your local bank office to order a new login card.")),
        TEMP_BLOCKED_CARD(new LocalizableKey("Too many incorrect tries. The card has been locked for 60 minutes for signing with the card reader without a cord. Contact Handelsbanken technical support for more information.")),
        TOO_MANY_ACTIVATED_APPS(new LocalizableKey("The activation could not be completed. It is not possible to activate more than ten apps."));

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
