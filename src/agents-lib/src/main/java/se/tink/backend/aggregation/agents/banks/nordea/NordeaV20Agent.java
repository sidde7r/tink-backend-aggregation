package se.tink.backend.aggregation.agents.banks.nordea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import org.apache.lucene.util.ThreadInterruptedException;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.nordea.utilities.Filters;
import se.tink.backend.aggregation.agents.banks.nordea.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.BankingServiceResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.CardDetailsEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.CardDetailsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ErrorMessage;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.InitialContextResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.LightLoginRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.LightLoginResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.LoginRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.TransactionListResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid.MobileBankIdAuthenticationResultResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid.MobileBankIdInitialAuthenticationRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid.MobileBankIdInitialAuthenticationRequestData;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid.MobileBankIdInitialAuthenticationResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.device.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.device.RegisterDeviceRequestData;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.device.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings.CustodyAccount;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings.CustodyAccountsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.LoginResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication.AuthenticationToken;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication.ChallengeResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication.RenewSecurityTokenResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication.StrongLoginRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication.StrongLoginResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries.BeneficiaryListResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.BankIdConfirmationRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.BankIdConfirmationResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.ChangePaymentRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.ChangePaymentResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.DeletePaymentsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.InitBankIdConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.InitBankIdPaymentRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.Payment;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentDetailsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentDetailsResponseOut;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentListResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.UnconfirmedPaymentListResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi.ConfirmPaymentsRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi.ConfirmPaymentsResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers.BankTransferRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class NordeaV20Agent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor,
        PersistentLogin {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_ATTEMPTS = 90;

    // Don't change! (requires migration)
    private final static String SENSITIVE_PAYLOAD_DEVICE_TOKEN = "deviceToken";
    private final static String SENSITIVE_PAYLOAD_PASSWORD = "password";

    // Don't change! (nordea types)
    private final static String PRODUCT_TYPE_ACCOUNT = "Account";
    private final static String PRODUCT_TYPE_CARD = "Card";
    private final static String PRODUCT_TYPE_LOAN = "Loan";

    private final static TypeReference<HashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {
    };

    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private static final ImmutableMap<String, MarketParameters> AVAILABLE_MARKETS = ImmutableMap.of(
            "SE", new MarketParameters("SE", ApiVersion.V23, "SEK", "2.6.0-554"),
            "FI", new MarketParameters("FI", ApiVersion.V21, "EUR", "2.0.1.100804"));

    private static final NordeaAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER = new NordeaAccountIdentifierFormatter();
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig
            .createWithMaxLength(50, 12, 50);

    private String securityToken;
    private final Client client;
    private final Credentials credentials;
    private final MarketParameters market;
    private final Catalog catalog;
    private final TransferMessageFormatter transferMessageFormatter;
    private final MetricRegistry register;
    private DateTime securityTokenLastUpdated;
    private final Set<String> availableAccountIds;
    private final HashMap<String, Double> custodyAccountCashValueMap;

    // cache
    private Map<ProductEntity, Account> productEntityAccountMap = null;
    private InitialContextResponse initialContextResponse = null;

    public NordeaV20Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.client = this.clientFactory.createCustomClient(context.getLogOutputStream());
        this.credentials = request.getCredentials();
        this.market = AVAILABLE_MARKETS.get(request.getProvider().getMarket());
        this.catalog = context.getCatalog();
        this.availableAccountIds = Sets.newHashSet();
        this.transferMessageFormatter = new TransferMessageFormatter(this.catalog,
                TRANSFER_MESSAGE_LENGTH_CONFIG, new StringNormalizerSwedish(".,?'-/:()+"));
        this.register = context.getMetricRegistry();
        custodyAccountCashValueMap = new HashMap<>();
    }

    /**
     * Helper method to parse a transaction entity.
     */
    private static Transaction parseTransaction(TransactionEntity te, AccountTypes accountType) {
        Transaction t = new Transaction();

        String dateString = (String) te.getTransactionDate().get("$");

        t.setDate(parseDate(dateString.substring(0, 10), true));
        t.setAmount(parseAmount(te.getTransactionAmount().get("$").toString()));

        if (te.getIsCoverReservationTransaction() != null && te.getIsCoverReservationTransaction().get("$") != null) {
            t.setPending((Boolean) te.getIsCoverReservationTransaction().get("$"));
        }

        String description = te.getTransactionText().get("$").toString();

        if (accountType == AccountTypes.CREDIT_CARD) {
            // Use counter-party name as transaction description for credit card accounts.

            if (te.getTransactionCounterpartyName() != null && te.getTransactionCounterpartyName().get("$") != null) {
                description = te.getTransactionCounterpartyName().get("$").toString();
            }

            // Detect pending transactions specifically for non-billed transactions.

            if (te.getCoverReservationTransaction() != null && te.getCoverReservationTransaction().get("$") != null) {
                t.setPending((Boolean) te.getCoverReservationTransaction().get("$"));
            } else if (Objects.equal(description, "KÖP") && te.getBilledTransaction() != null
                    && te.getBilledTransaction().get("$") != null && !((Boolean) te.getBilledTransaction().get("$"))) {
                t.setPending(true);
            }

            // Invert credit card transactions.

            t.setAmount(-t.getAmount());

            if (t.getAmount() < 0) {
                t.setType(TransactionTypes.CREDIT_CARD);
            }
        }

        NordeaAgentUtils.parseTransactionDescription(CharMatcher.WHITESPACE.trimFrom(description), t);

        NordeaAgentUtils.parseTransactionTypeForFI(te.getTransactionTypeForFi(), t);

        return t;
    }

    private String authenticateSE() throws AuthenticationException, AuthorizationException {

        String authenticationToken = null;

        // TODO: Replace with new AgentWorker
        if (!(this.request.getType() == CredentialsRequestType.TRANSFER) && (this.request.isUpdate() || this.request
                .isCreate())) {
            // if (request instanceof UpdateCredentialsRequest || request instanceof CreateCredentialsRequest) {
            // Start off in manual mode (Bank ID)
            updateCredentialsType(CredentialsTypes.MOBILE_BANKID);
            authenticationToken = authenticateManually();

            String password = this.credentials.getField(Field.Key.PASSWORD);
            if (!Strings.isNullOrEmpty(authenticationToken) && !Strings.isNullOrEmpty(password)) {
                // Save the password
                this.credentials.setSensitivePayload(SENSITIVE_PAYLOAD_PASSWORD, password);
                // Reqister the device
                String deviceId = getDeviceId();
                String deviceToken = registerDevice(deviceId, authenticationToken);

                // Set the credentials to be automatically updated and authenticate the registered device
                updateCredentialsType(CredentialsTypes.PASSWORD);
                authenticationToken = authenticateRegisteredDevice(deviceId, deviceToken);
            }
        } else {
            if (this.credentials.getType() == CredentialsTypes.MOBILE_BANKID) {
                // Manual refresh
                authenticationToken = authenticateManually();
            } else if (!Strings.isNullOrEmpty(this.credentials.getSensitivePayload(SENSITIVE_PAYLOAD_PASSWORD))) {
                // Automatic refresh
                authenticationToken = authenticateAutomatically();
            } else {
                this.log.warn("Automatic refresh, but has no password.");
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }

        return authenticationToken;
    }

    private String authenticateFI() throws AuthenticationException, AuthorizationException {
        Credentials credentials = this.request.getCredentials();
        LightLoginRequest lightLoginRequest = new LightLoginRequest();
        lightLoginRequest.getUserId().put("$", credentials.getUsername());
        lightLoginRequest.getPassword().put("$", credentials.getPassword());
        lightLoginRequest.getType().put("$", "lightLoginFI");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLightLoginRequest(lightLoginRequest);

        LoginResponse loginResponse;
        try {
            String loginResponseContent = createClientRequest(
                    this.market.getAuthenticationEndPoint() + "/SecurityToken",
                    null).type(MediaType.APPLICATION_JSON_TYPE).post(String.class, loginRequest);

            loginResponse = MAPPER.readValue(loginResponseContent, LoginResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        Preconditions.checkNotNull(loginResponse);
        Preconditions.checkNotNull(loginResponse.getLightLoginResponse());

        LightLoginResponse response = loginResponse.getLightLoginResponse();

        if (response.getErrorMessage() != null) {
            String errorCode = (String) response.getErrorMessage().getErrorCode().get("$");
            this.log.info(String.format("Bank ID light login failed (%s)", errorCode));
            NordeaErrorUtils.throwError(errorCode);
        }

        // Grab the security token for the session.

        return (String) response.getAuthenticationToken().getToken().get("$");
    }

    // Create a faux device-ID.
    private String getDeviceId() {
        return StringUtils.hashAsUUID("TINK-" + this.request.getCredentials().getUsername());
    }

    private String authenticateAutomatically() throws AuthenticationException, AuthorizationException {
        String deviceId = getDeviceId();
        String deviceToken = authenticateDevice(deviceId);

        String authenticationToken = null;

        if (!Strings.isNullOrEmpty(deviceToken)) {
            authenticationToken = authenticateRegisteredDevice(deviceId, deviceToken);
        }

        return authenticationToken;
    }

    private String authenticateRegisteredDevice(String deviceId, String deviceToken)
            throws AuthenticationException, AuthorizationException {

        String authenticationToken = null;

        LightLoginRequest lightLoginRequest = new LightLoginRequest();
        lightLoginRequest.getPassword().put("$", this.credentials.getSensitivePayload(SENSITIVE_PAYLOAD_PASSWORD));
        lightLoginRequest.getType().put("$", "lightLogin" + this.market.getMarketCode());
        lightLoginRequest.getDeviceRegistrationToken().put("$", deviceToken);
        lightLoginRequest.getDeviceId().put("$", deviceId);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLightLoginRequest(lightLoginRequest);

        LoginResponse loginResponse;
        try {
            String loginResponseContent = createClientRequest(
                    this.market.getAuthenticationEndPoint() + "/SecurityToken",
                    null).type(MediaType.APPLICATION_JSON_TYPE).post(String.class, loginRequest);

            loginResponse = MAPPER.readValue(loginResponseContent, LoginResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (loginResponse != null && loginResponse.getLightLoginResponse() != null) {
            LightLoginResponse response = loginResponse.getLightLoginResponse();

            if (response.getErrorMessage() != null) {
                String errorCode = (String) response.getErrorMessage().getErrorCode().get("$");
                this.log.info(String.format("Bank ID light login failed (%s)", errorCode));
                NordeaErrorUtils.throwError(errorCode);
            }

            // Grab the security token for the session.
            authenticationToken = (String) response.getAuthenticationToken().getToken().get("$");

        } else {
            Preconditions.checkNotNull(
                    loginResponse);
            Preconditions.checkNotNull(
                    loginResponse.getBankingServiceResponse());
            Preconditions.checkNotNull(
                    loginResponse.getBankingServiceResponse().getErrorMessage());
            Preconditions.checkState(
                    loginResponse.getBankingServiceResponse().getErrorMessage().containsKey("errorCode"));

            // Error code available
            String code = loginResponse.getBankingServiceResponse().getErrorMessage().get("errorCode").toString();
            NordeaErrorUtils.throwError(code);
        }

        return authenticationToken;
    }

    private MobileBankIdAuthenticationResultResponse authenticateBankIdConfirm(String requestToken,
            String authenticationToken) {
        try {
            String resultResponseContent = createClientRequest(
                    this.market.getBankingEndpoint() + "/MobileBankIdAuthenticationResult/" + requestToken,
                    authenticationToken)
                    .get(String.class);

            return MAPPER.readValue(resultResponseContent, MobileBankIdAuthenticationResultResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private MobileBankIdInitialAuthenticationResponse authenticateBankIdInitialize() {

        MobileBankIdInitialAuthenticationRequestData initBankIdRequest = new MobileBankIdInitialAuthenticationRequestData();
        initBankIdRequest.getUserId().put("$", this.credentials.getUsername());

        MobileBankIdInitialAuthenticationRequest bankIdRequest = new MobileBankIdInitialAuthenticationRequest();
        bankIdRequest.setData(initBankIdRequest);

        String initRequestContent;
        try {
            initRequestContent = MAPPER.writeValueAsString(bankIdRequest);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        String initResponseContent = createClientRequest(
                this.market.getBankingEndpoint() + "/MobileBankIdInitialAuthentication", null)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, initRequestContent);

        try {
            return MAPPER.readValue(initResponseContent, MobileBankIdInitialAuthenticationResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    private RegisterDeviceResponse authenticateBankIdRegisterDevice(String deviceId, String authenticationToken) {

        RegisterDeviceRequestData registerDeviceRequestData = new RegisterDeviceRequestData();
        registerDeviceRequestData.getDeviceId().put("$", deviceId);

        RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest();
        registerDeviceRequest.setData(registerDeviceRequestData);

        String registerDeviceRequestContent;
        try {
            registerDeviceRequestContent = MAPPER.writeValueAsString(registerDeviceRequest);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        String registerDeviceResponseContent = createClientRequest(
                this.market.getAuthenticationEndPoint() + "/RegisterDevice",
                authenticationToken).type(MediaType.APPLICATION_JSON_TYPE).post(String.class,
                registerDeviceRequestContent);

        try {
            return MAPPER.readValue(registerDeviceResponseContent, RegisterDeviceResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String registerDevice(String deviceId) throws AuthenticationException, AuthorizationException {
        String authenticationToken = authenticateManually();
        return registerDevice(authenticationToken, deviceId);
    }

    private String registerDevice(String deviceId, String authenticationToken) {

        String deviceToken = null;

        if (!Strings.isNullOrEmpty(authenticationToken)) {
            // Register device persistently and use the token instead manual authentication in the future.
            RegisterDeviceResponse registerDeviceResponse = authenticateBankIdRegisterDevice(deviceId,
                    authenticationToken);
            deviceToken = (String) registerDeviceResponse.getData().getDeviceRegistrationToken().get("$");
            this.credentials.setSensitivePayload(SENSITIVE_PAYLOAD_DEVICE_TOKEN, deviceToken);
        }

        return deviceToken;
    }

    private String authenticateDevice(String deviceId) throws AuthenticationException, AuthorizationException {

        String deviceToken = this.credentials.getSensitivePayload(SENSITIVE_PAYLOAD_DEVICE_TOKEN);

        if (Strings.isNullOrEmpty(deviceToken)) {
            deviceToken = registerDevice(deviceId);
        }

        return deviceToken;
    }

    private String authenticateManually() throws AuthenticationException, AuthorizationException {
        // If we're not in manual mode, we can't do anything until the user manually updates the credentials.

        if (!this.request.isManual()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // Initialize the authentication process.

        MobileBankIdInitialAuthenticationResponse initResponse = authenticateBankIdInitialize();
        BankingServiceResponse initServiceResponse = initResponse.getServiceResponse();

        // Initialization failed.
        if (initServiceResponse != null && initServiceResponse.getErrorMessage() != null) {
            String errorCode = (String) initServiceResponse.getErrorMessage().getErrorCode().get("$");
            this.log.info(String.format("Bank ID initial authentication failed (%s)", errorCode));
            NordeaErrorUtils.throwError(errorCode);
        }

        // Request the client to perform Bank ID authentication.

        this.credentials.setSupplementalInformation(null);
        this.credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        this.context.requestSupplementalInformation(this.credentials, false);

        // Confirm that Nordea authenticated the the account.

        String requestToken = (String) initResponse.getData().getBankIdAuthenticationRequestToken().get("$");
        String authenticationToken = (String) initResponse.getData().getAuthenticationToken().getToken().get("$");

        MobileBankIdAuthenticationResultResponse resultResponse = null;
        BankingServiceResponse resultServiceResponse = null;

        String progressStatus = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            resultResponse = authenticateBankIdConfirm(requestToken, authenticationToken);
            resultServiceResponse = resultResponse.getServiceResponse();

            if (resultServiceResponse != null && resultServiceResponse.getErrorMessage() != null) {
                break;
            }

            progressStatus = (String) resultResponse.getData().getProgressStatus().get("$");

            // Break upon completion. Otherwise try again.
            if ("COMPLETE".equals(progressStatus)) {
                break;
            } else {
                this.log.info(String.format("Bank ID confirmation not complete (%s)", progressStatus));
            }

            // Wait for 2 seconds before trying again (if you have any attempts left).
            if (i < (MAX_ATTEMPTS - 1)) {
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            }
        }

        // Bank ID authentication finished with errors.
        if (resultServiceResponse != null && resultServiceResponse.getErrorMessage() != null) {
            String errorCode = (String) resultServiceResponse.getErrorMessage().getErrorCode().get("$");
            this.log.info(String.format("Bank ID confirmation failed (%s)", errorCode));
            NordeaErrorUtils.throwError(errorCode);
        }

        // Bank ID authentication finished before completion.
        Preconditions.checkState(
                Objects.equal(progressStatus, "COMPLETE"),
                "Bank ID authentication finished prematurely (%s)", progressStatus);

        // Bank ID authentication was successfully completed. The token should be same as before...
        authenticationToken = (String) resultResponse.getData().getAuthenticationToken().getToken().get("$");

        return authenticationToken;
    }

    /**
     * Helper method to construct a client request.
     */
    private Builder createClientRequest(String url, String securityToken) {
        int requestId = ThreadLocalRandom.current().nextInt(100000);

        Builder request = this.client.resource(url).accept("*/*")
                .header("User-Agent", DEFAULT_USER_AGENT)
                .header("x-Request-Id", Integer.toString(requestId))
                .header("x-Platform-Version", "8.1.3")
                .header("x-App-Country", this.market.getMarketCode())
                .header("x-App-Language", this.market.getMarketCode())
                .header("x-App-Version", this.market.getAppVersion())
                .header("x-App-Name", "MBA")
                .header("x-Device-Make", "Tink")
                .header("x-Device-Model", "Tink")
                .header("x-Platform-Type", "iOS");

        if (securityToken != null) {
            request = request.header("x-Security-Token", securityToken);
        }

        return request;
    }

    private String parseTransactionKey(TransactionEntity te) {
        if (te.getTransactionKey().get("$") == null) {
            return StringUtils.hashAsStringMD5(te.getTransactionText().get("$").toString()
                    + te.getTransactionDate().get("$").toString() + te.getTransactionAmount().get("$").toString());
        } else {
            return te.getTransactionKey().get("$").toString();
        }
    }

    /**
     * Helper method to parse transactions from a transaction list response.
     */
    private Map<String, Transaction> parseTransactions(TransactionListResponse transactionListResponse,
            AccountTypes accountType) {
        Map<String, Transaction> transactions = Maps.newHashMap();

        if (transactionListResponse.getAccountTransactions() != null
                && transactionListResponse.getAccountTransactions().getAccountTransactions() != null) {
            for (TransactionEntity te : transactionListResponse.getAccountTransactions().getAccountTransactions()) {
                if (this.market.getMarketCode().equals("FI")) {
                    te.parseTransactionTextForFi();
                }

                transactions.put(parseTransactionKey(te), parseTransaction(te, accountType));
            }
        }

        if (transactionListResponse.getCreditCardTransactions() != null
                && transactionListResponse.getCreditCardTransactions().getTransactions() != null) {
            for (TransactionEntity te : transactionListResponse.getCreditCardTransactions().getTransactions()) {
                if (this.market.getMarketCode().equals("FI")) {
                    te.parseTransactionTextForFi();
                }

                transactions.put(parseTransactionKey(te), parseTransaction(te, accountType));
            }
        }

        return transactions;
    }

    private Optional<Account> constructAccount(ProductEntity productEntity) throws IOException {
        Account account = new Account();

        if (productEntity.getBalance() != null && productEntity.getBalance().containsKey("$")) {
            account.setBalance(parseAmount(productEntity.getBalance().get("$").toString()));
        }

        if (productEntity.getNickName() != null && productEntity.getNickName().containsKey("$")) {
            account.setName(productEntity.getNickName().get("$").toString());
        }

        account.setAccountNumber(productEntity.getAccountNumber(true));
        account.setBankId(NordeaAgentUtils.maskAccountNumber(productEntity.getAccountNumber(false)));
        if (productEntity.canReceiveInternalTransfer() && productEntity.canMakeInternalTransfer()) {
            account.putIdentifier(new SwedishIdentifier(productEntity.getAccountNumber(true)));
        }

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches(
                        REGEXP_OR_JOINER.join("\\*{12}[0-9]{4}", "Classic", "\\*{12}-[0-9]{3}")),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        String accountTypeCode = productEntity.getNordeaProductTypeExtension();

        String accountName = NordeaAgentUtils.getAccountNameForCode(accountTypeCode);

        if (accountName != null && account.getName() == null) {
            account.setName(accountName);
        }

        if (account.getName() == null) {
            account.setName(account.getAccountNumber());
        }

        // Determine the account type.
        AccountTypes accountType = NordeaAgentUtils.getAccountTypeForCode(accountTypeCode);

        if (accountType == null) {
            this.log.warn(String.format("Unknown account-product type: %s", accountTypeCode));
            accountType = AccountTypes.CHECKING;
        }

        account.setType(accountType);

        String currency = "";

        if (productEntity.getCurrency().containsKey("$")) {
            currency = productEntity.getCurrency().get("$").toString();
        }

        if (!currency.equalsIgnoreCase(this.market.getCurrency())) {
            this.log.warn(String.format(
                    "%s is the only supported currency for Nordea. Account will not be imported. Account currency was %s",
                    this.market.getCurrency(), currency));
            return Optional.empty();
        }

        String productType = productEntity.getProductType().get("$").toString();

        if (Objects.equal(productType, PRODUCT_TYPE_CARD)) {
            String accountId = productEntity.getNordeaAccountIdV2();

            String cardDetailsResponseContent = createClientRequest(
                    this.market.getBankingEndpoint() + "/Cards/" + accountId,
                    this.securityToken).get(String.class);

            CardDetailsResponse cardDetailsResponse = MAPPER.readValue(cardDetailsResponseContent,
                    CardDetailsResponse.class);

            CardDetailsEntity cardDetails = cardDetailsResponse.getCardDetails();

            Double balance = cardDetails.getCurrentBalance();

            if (balance != null) {
                account.setBalance(balance);
            }

            if (cardDetails.getFundsAvailable().containsKey("$")) {
                account.setAvailableCredit(parseAmount(cardDetails.getFundsAvailable().get("$").toString()));
            }
        }

        return Optional.ofNullable(account);
    }

    /**
     * Fetch information about an account/product.
     */
    private void refreshAccountsAndTransactions(ProductEntity productEntity, Account account) throws IOException {
        String productType = productEntity.getNordeaProductType();
        String accountId = productEntity.getNordeaAccountIdV2();

        if (Objects.equal(productType, PRODUCT_TYPE_ACCOUNT)) {
            refreshAccountTransactions(account, accountId, productEntity);
        } else if (Objects.equal(productType, PRODUCT_TYPE_CARD)) {
            refreshCreditCardAccountTransactions(account, accountId);
        }
    }

    /**
     * Fetch regular account transactions.
     */
    private Account refreshAccountTransactions(Account account, String accountId, ProductEntity productEntity)
            throws IOException {
        Map<String, Transaction> transactionsMap = Maps.newHashMap();
        List<Transaction> transactions = Lists.newArrayList();

        String continueKey = "";

        // Fetch all the transaction pages.

        while (true) {
            TransactionListResponse transactionListResponse = fetchTransactions(accountId, continueKey);

            // We've gotten some temporary error from Nordea when we try to get the accounts.
            // Because of this we'll try to fetch again and see if we succeed.
            if (transactionListResponse.getAccountTransactions() == null) {
                this.log.warn("Failed fetching transactions. Retrying...");
                transactionListResponse = fetchTransactions(accountId, continueKey);

                Preconditions.checkNotNull(transactionListResponse.getAccountTransactions(),
                        "Failed retry fetching transactions.");

            }

            continueKey = (transactionListResponse.getAccountTransactions().getContinueKey().get("$") == null ? null
                    : transactionListResponse.getAccountTransactions().getContinueKey().get("$").toString());

            Map<String, Transaction> transactionsPage = parseTransactions(transactionListResponse, account.getType());

            // Break if we've fetched an empty transaction page.

            if (!transactionsPage.isEmpty()) {
                transactionsMap.putAll(transactionsPage);
                transactions = Lists.newArrayList(transactionsMap.values());

                this.context.updateStatus(CredentialsStatus.UPDATING, account, transactions);

                // See if we're content with the data we have.

                if (isContentWithRefresh(account, transactions)) {
                    break;
                }
            }

            if (Strings.isNullOrEmpty(continueKey)) {
                break;
            }
        }

        List<PaymentPair> upcomingPayments = getPaymentsWithDetails(productEntity, Payment.StatusCode.CONFIRMED);
        List<Transaction> upcomingTransactions = populateUpcomingTransactions(upcomingPayments);

        transactions.addAll(upcomingTransactions);

        this.context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        return this.context.updateTransactions(account, NordeaAgentUtils.TRANSACTION_ORDERING.reverse()
                .sortedCopy(transactions));
    }

    private TransactionListResponse fetchTransactions(String accountId, String continueKey) throws IOException {
        String transactionListResponseContent = createClientRequest(
                this.market.getBankingEndpoint() + "/Transactions?accountId=" + accountId + "&continueKey="
                        + continueKey,
                this.securityToken).get(String.class);

        return MAPPER.readValue(transactionListResponseContent, TransactionListResponse.class);
    }

    private List<Transaction> populateUpcomingTransactions(List<PaymentPair> upcomingPayments)
            throws JsonProcessingException {

        List<Transaction> transactions = Lists.newArrayList();

        for (PaymentPair upcomingPayment : upcomingPayments) {
            Optional<Transfer> transferOptional = upcomingPayment.toModifiableTransfer();

            Transaction transaction = upcomingPayment.toTransaction(transferOptional);
            transactions.add(transaction);
        }

        return transactions;
    }

    /**
     * Fetch credit card transactions.
     */
    private Account refreshCreditCardAccountTransactions(Account account, String accountId)
            throws IOException {

        Map<String, Transaction> transactionsMap = Maps.newHashMap();
        List<Transaction> transactionsList = Lists.newArrayList();

        Calendar startDateOfMonth = null;

        // Loop through all available transaction pages.

        while (true) {
            String period = (startDateOfMonth == null ? null : DateUtils.getMonthPeriod(startDateOfMonth.getTime()));

            String beginTransactionDate = (period == null ? "" : ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils
                    .getFirstDateFromPeriod(period)));
            String endTransactionDate = (period == null ? "" : ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils
                    .getLastDateFromPeriod(period)));

            String transactionListResponseContent = createClientRequest(
                    this.market.getBankingEndpoint() + "/Transactions?cardNumber=" + accountId
                            + "&beginTransactionDate="
                            + beginTransactionDate + "&endTransactionDate=" + endTransactionDate, this.securityToken)
                    .get(
                            String.class);

            TransactionListResponse transactionListResponse = MAPPER.readValue(transactionListResponseContent,
                    TransactionListResponse.class);

            Map<String, Transaction> transactionsPage = parseTransactions(transactionListResponse, account.getType());

            // Break if we've fetched an empty transaction page.

            if (period != null && transactionsPage.isEmpty()) {
                break;
            } else {
                transactionsMap.putAll(transactionsPage);
                transactionsList = Lists.newArrayList(transactionsMap.values());

                this.context.updateStatus(CredentialsStatus.UPDATING, account, transactionsList);

                // Either construct the date object to start fetching the historical card transactions, or go back
                // another month.

                if (startDateOfMonth == null) {
                    startDateOfMonth = DateUtils.getCalendar();
                    startDateOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    startDateOfMonth.add(Calendar.MONTH, -1);
                }

                // See if we're content with the data we have.

                if (isContentWithRefresh(account, transactionsList)) {
                    break;
                } else {
                    continue;
                }
            }
        }

        this.context.updateStatus(CredentialsStatus.UPDATING, account, transactionsList);
        return this.context.updateTransactions(account, NordeaAgentUtils.TRANSACTION_ORDERING.reverse()
                .sortedCopy(transactionsList));
    }

    private void refreshLoan(Account account, ProductEntity product) throws IOException, ParseException {
        String accountId = product.getNordeaAccountIdV2();

        String loanResponseContent = createClientRequest(
                this.market.getBankingEndpoint() + "/Loans/Details/" + accountId,
                this.securityToken).get(String.class);
        LoanDetailsResponse loanDetails = MAPPER.readValue(loanResponseContent, LoanDetailsResponse.class);

        AccountFeatures assets = AccountFeatures.createEmpty();

        if (loanDetails != null && loanDetails.getLoanDetails() != null &&
                loanDetails.getLoanDetails().getLoanData() != null) {

            Loan.Type loanType = NordeaAgentUtils.getLoanTypeForCode(product.getNordeaProductTypeExtension());

            Loan loan = loanDetails.toLoan(account, loanType, loanResponseContent);
            assets.setLoans(Lists.newArrayList(loan));
        }

        this.context.cacheAccount(account, assets);
    }

    private void refreshInvestmentAccounts() throws IOException {

        CustodyAccountsResponse response = createClientRequest(this.market.getSavingsEndpoint() + "/CustodyAccounts",
                this.securityToken)
                .type(MediaType.APPLICATION_JSON_TYPE).get(CustodyAccountsResponse.class);

        // The Custody Account Service at Nordea isn't very stable. Returning silently from error "MBS0110" or "MBS9001"
        // (Your custody accounts cannot be shown at the moment. Please try again later) as it is more important
        // to get other accounts and transactions updated compared to having data for the custody accounts

        if (response.hasError()) {
            if (response.getErrorCode().equals("MBS0110") || response.getErrorCode().equals("MBS9001")) {
                this.log.warn("Could not fetch custody accounts from Nordea");
                return;
            }

            updateStatus(response.getErrorCode());
            return;
        }

        for (CustodyAccount custodyAccount : response.getCustodyAccounts()) {
            try {

                if (custodyAccount == null) {
                    continue;
                }

                Preconditions.checkState(custodyAccount.hasValidBankId(),
                        "Unexpected account.bankid '%s' for account.name '%s'. Reformatted?",
                        custodyAccount.getAccountId(), custodyAccount.getName());

                if (!custodyAccount.getCurrency().equalsIgnoreCase(this.market.getCurrency())) {
                    this.log.warn(String.format("%s is the only supported currency. Currency was %s",
                            this.market.getCurrency(), custodyAccount.getCurrency()));
                    return;
                }

                String accountNumber = StringUtils.removeNonAlphaNumeric(custodyAccount.getAccountNumber());

                Account account = custodyAccount.toAccount();
                Portfolio portfolio = custodyAccount.toPortfolio(
                        custodyAccountCashValueMap.getOrDefault(accountNumber, 0.0));

                List<Instrument> instruments = Lists.newArrayList();
                custodyAccount.getHoldings()
                        .forEach(holdingsEntity -> {
                            holdingsEntity.toInstrument(custodyAccount.getCurrency())
                                    .ifPresent(instruments::add);
                        });
                portfolio.setInstruments(instruments);

                this.context.cacheAccount(account, AccountFeatures.createForPortfolios(portfolio));
            } catch (Exception e) {
                // Don't fail the whole refresh just because we failed updating investment data but log error.
                this.log.error("Caught exception while updating investment data", e);
            }
        }
    }

    private void updateCredentialsType(CredentialsTypes type) {
        if (type != this.credentials.getType()) {
            this.credentials.setType(type);
            this.context.updateCredentialsExcludingSensitiveInformation(this.credentials, false);
        }
    }

    /**
     * Helper method to update the status based on error messages from Nordea.
     */
    private void updateStatus(String code) {
        try {

            this.context.updateStatus(NordeaErrorUtils.getErrorStatus(code), NordeaErrorUtils.getErrorMessage(code));
        } catch (Exception e) {
            this.context.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            this.log.error("Could not update status", e);
        }
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        // Authenticate the user. Sweden and Finland have different authentication methods.

        this.securityToken = null;

        switch (this.market.getMarketCode()) {
        case "SE":
            this.securityToken = authenticateSE();
            break;
        case "FI":
            this.securityToken = authenticateFI();
            break;
        default:
            throw new IllegalStateException("Unknown market, authentication not implemented");
        }

        Preconditions.checkNotNull(this.securityToken);
        return true;
    }

    @Override
    public void logout() throws Exception {
        // todo: not implemented
    }

    private Map<ProductEntity, Account> getAccounts() {
        if (productEntityAccountMap != null) {
            return productEntityAccountMap;
        }

        productEntityAccountMap = new HashMap<>();
        try {
            InitialContextResponse contextResponse = getInitialContext();
            CustodyAccountsResponse custodyAccountsResponse = createClientRequest(
                    this.market.getSavingsEndpoint() + "/CustodyAccounts",
                    this.securityToken)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .get(CustodyAccountsResponse.class);

            custodyAccountsResponse.getCustodyAccounts()
                    .stream()
                    .map(CustodyAccount::getAccountNumber)
                    .map(StringUtils::removeNonAlphaNumeric)
                    .forEach(number -> custodyAccountCashValueMap.put(number, 0.0));

            if (contextResponse == null) {
                return Collections.emptyMap();
            }

            for (ProductEntity productEntity : contextResponse
                    .getProductsOfTypes(PRODUCT_TYPE_ACCOUNT, PRODUCT_TYPE_CARD)) {

                // If account belongs to a custody account we store it so that it can represent the
                // cash value of that investment. We skip constructing an account from it.
                // This value is retrieved in this::refreshInvestmentAccounts.
                if (productEntity.getProductNumber() != null && productEntity.getProductNumber().containsKey("$")) {

                    String productNumber = productEntity.getProductNumber().get("$").toString();
                    if (custodyAccountCashValueMap.containsKey(productNumber)) {

                        Double cashValue = parseAmount(productEntity
                                .getBalance()
                                .get("$")
                                .toString());
                        custodyAccountCashValueMap.put(productNumber, cashValue);
                        continue;
                    }
                }

                Optional<Account> account = constructAccount(productEntity);

                if (!account.isPresent()) {
                    continue;
                }

                productEntityAccountMap.put(productEntity, account.get());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return productEntityAccountMap;
    }

    private void updateAccountsPerType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> context.cacheAccount(set.getValue()));
    }

    private void updateTransactionsPerAccountType(RefreshableItem type) {
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .forEach(set -> {
                    try {
                        refreshAccountsAndTransactions(set.getKey(), set.getValue());
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @Override
    public void refresh(RefreshableItem item) {

        // cached
        InitialContextResponse contextResponse = getInitialContext();
        if (contextResponse == null) {
            return;
        }

        switch (item) {
        case TRANSFER_DESTINATIONS:
            try {
                updateTransferDestinations();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case EINVOICES:
            try {
                updateEInvoices();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
        case CREDITCARD_ACCOUNTS:
            updateAccountsPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
            updateTransactionsPerAccountType(item);
            break;

        case LOAN_ACCOUNTS:
            try {
                refreshLoans(contextResponse);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                refreshInvestmentAccounts();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;
        }
    }

    private void refreshLoans(InitialContextResponse contextResponse) throws IOException {

        List<ProductEntity> products = contextResponse.getProductsOfTypes(PRODUCT_TYPE_LOAN);

        for (ProductEntity product : products) {

            Optional<Account> account = constructAccount(product);

            if (!account.isPresent()) {
                continue;
            }

            try {
                refreshLoan(account.get(), product);
            } catch (Exception e) {
                this.log.error("Couldn't fetch loan information--ignoring account ("
                        + product.getNordeaAccountIdV2() + ")", e);
            }
        }
    }

    private void updateTransferDestinations() throws Exception {
        InitialContextResponse contextResponse = getInitialContext();

        if (contextResponse == null) {
            return;
        }

        List<GeneralAccountEntity> sourceAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> destinationAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> paymentSourceAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> paymentDestinationAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> internalOnlySourceAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> internalOnlyDestinationAccounts = Lists.newArrayList();

        List<ProductEntity> internalAccounts = contextResponse.getData().getProducts();
        List<BeneficiaryEntity> beneficiaries = getBeneficiaries();

        if (internalAccounts == null || beneficiaries == null) {
            return;
        }

        for (ProductEntity entity : internalAccounts) {

            Boolean ownTransferFrom = entity.getProductIdBoolean("@ownTransferFrom");
            Boolean ownTransferTo = entity.getProductIdBoolean("@ownTransferTo");
            Boolean thirdParty = entity.getProductIdBoolean("@thirdParty");

            if (ownTransferFrom != null && ownTransferFrom) {
                if (thirdParty != null && thirdParty) {
                    sourceAccounts.add(entity);
                } else {
                    internalOnlySourceAccounts.add(entity);
                }

                if (entity.canMakePayment()) {
                    paymentSourceAccounts.add(entity);
                }
            }

            if (ownTransferTo != null && ownTransferTo) {
                destinationAccounts.add(entity);
                internalOnlyDestinationAccounts.add(entity);
            }
        }

        for (BeneficiaryEntity entity : beneficiaries) {
            if (entity.isBankTransferEntity()) {
                destinationAccounts.add(entity);
            }

            if (entity.isPaymentEntity()) {
                if (entity.isPgPaymentEntity() || entity.isBgPaymentEntity()) {
                    paymentDestinationAccounts.add(entity);
                }
            }
        }

        TransferDestinationsResponse response = new TransferDestinationsResponse();

        Map<Account, List<TransferDestinationPattern>> internalOnly = new TransferDestinationPatternBuilder()
                .setTinkAccounts(context.getUpdatedAccounts())
                .setSourceAccounts(internalOnlySourceAccounts)
                .setDestinationAccounts(internalOnlyDestinationAccounts)
                .build();

        response.addDestinations(internalOnly);

        Map<Account, List<TransferDestinationPattern>> external = new TransferDestinationPatternBuilder()
                .setTinkAccounts(context.getUpdatedAccounts())
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();

        response.addDestinations(external);

        Map<Account, List<TransferDestinationPattern>> payments = new TransferDestinationPatternBuilder()
                .setTinkAccounts(context.getUpdatedAccounts())
                .setSourceAccounts(paymentSourceAccounts)
                .setDestinationAccounts(paymentDestinationAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .build();

        response.addDestinations(payments);

        context.updateTransferDestinationPatterns(response.getDestinations());
    }

    private void updateEInvoices() throws Exception {
        if (!this.market.getMarketCode().equals("SE")) {
            return;
        }

        InitialContextResponse contextResponse = getInitialContext();

        if (contextResponse == null) {
            return;
        }

        List<ProductEntity> accounts = contextResponse.getData().getProducts();

        List<PaymentPair> unsignedEInvoices = getPaymentsWithDetails(accounts,
                Payment.StatusCode.UNCONFIRMED, Payment.SubType.EINVOICE);

        List<Transfer> eInvoices = Lists.newArrayList(FluentIterable
                .from(unsignedEInvoices)
                .transform(PaymentPair.TO_PAYMENTDETAILS)
                .transform(PaymentDetailsResponseOut.TO_EINVOICE_TRANSFER));

        context.updateEinvoices(eInvoices);
    }

    private InitialContextResponse getInitialContext() {
        if (initialContextResponse != null) {
            return initialContextResponse;
        }

        try {
            String contextResponseContent = createClientRequest(
                    this.market.getBankingEndpoint() + "/initialContext", this.securityToken)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);

            InitialContextResponse contextResponse = MAPPER.readValue(contextResponseContent,
                    InitialContextResponse.class);

            BankingServiceResponse serviceResponse = contextResponse.getBankingServiceResponse();

            if (serviceResponse != null && serviceResponse.getErrorMessage() != null) {
                String errorCode = (String) serviceResponse.getErrorMessage().getErrorCode().get("$");
                this.log.info(String.format("Initial context could not be fetched (%s)", errorCode));
                updateStatus(errorCode);
                return null;
            }

            initialContextResponse = contextResponse;
            return initialContextResponse;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private List<BeneficiaryEntity> getBeneficiaries() throws IOException {

        String url = this.market.getBankingEndpoint() + "/Beneficiaries";

        BeneficiaryListResponse response = createJsonRequest(url, BeneficiaryListResponse.class);

        return response.getBeneficiaryListOut().getBeneficiaries();
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(this.client);
    }

    @Override
    public void execute(Transfer transfer) throws Exception,
            TransferExecutionException {
        // Ensure that only Nordea-SE users are allowed to make transfers
        if (!Objects.equal(this.market.getMarketCode(), "SE")) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Transfers are not allowed for market (%s)", this.market.getMarketCode()))
                    .build();
        }

        switch (transfer.getType()) {
        case BANK_TRANSFER:
            executeBankTransfer(transfer);
            break;
        case PAYMENT:
            executePayment(transfer);
            break;
        case EINVOICE:
            throw new IllegalStateException("Should never happen, since eInvoices are run through update call");
        default:
            throw new IllegalStateException("Not implemented");
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {
        // Ensure that only Nordea-SE users are allowed to make payments
        if (!Objects.equal(this.market.getMarketCode(), "SE")) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(String.format("Payments are not allowed for market (%s)", this.market.getMarketCode()))
                    .build();
        }

        switch (transfer.getType()) {
        case EINVOICE:
            approveEInvoice(transfer, Payment.StatusCode.UNCONFIRMED);
            break;
        case BANK_TRANSFER:
            throw new IllegalStateException("Not implemented");
        case PAYMENT:
            approveEInvoice(transfer, Payment.StatusCode.CONFIRMED);
            break;
        default:
            throw new IllegalStateException("Should never happen, not recognized transfer type");
        }
    }

    private void approveEInvoice(Transfer transfer, Payment.StatusCode statusCode)
            throws TransferExecutionException, InterruptedException, IOException {
        Preconditions.checkState(Objects.equal(this.market.getMarketCode(), "SE"),
                String.format("Payments not implemented for market(%s)", this.market.getMarketCode()));
        List<ProductEntity> paymentAccounts = getPaymentAccounts();

        Transfer originalTransfer = getOriginalTransfer(transfer);
        PaymentPair matchingEInvoice = fetchMatchingEInvoice(paymentAccounts, originalTransfer, statusCode);

        if (isTransferModifyingEInvoice(matchingEInvoice.getPaymentDetails(), transfer)) {
            matchingEInvoice = updateEInvoice(paymentAccounts, matchingEInvoice.getPaymentDetails(), transfer);
        }

        signEInvoice(matchingEInvoice.getPaymentEntity());
    }

    private List<ProductEntity> getPaymentAccounts() throws IOException {
        InitialContextResponse contextResponse = getInitialContext();

        Preconditions.checkNotNull(contextResponse);
        Preconditions.checkNotNull(contextResponse.getData());
        Preconditions.checkNotNull(contextResponse.getData().getProducts());

        ImmutableList<ProductEntity> paymentAccounts = FluentIterable
                .from(contextResponse.getData().getProducts())
                .filter(NordeaAgentUtils.PRODUCT_CAN_MAKE_PAYMENT)
                .toList();

        Preconditions.checkState(!paymentAccounts.isEmpty());

        return paymentAccounts;
    }

    /**
     * We both need to check the hash of the overview item (preferably first, and then go over details for each, since
     * those contains all information needed)
     * <p>
     * Reason not to look for PAYMENTs as well is that Nordea afaik changes state on a NordeaPayment as soon as it's
     * updated to unconfirmed and it then will be confirmed again after signing succeeds.
     * <p>
     * So since the user don't get upcoming unconfirmed PAYMENTs in his/her feed to confirm there is no way for the user
     * to recover from a failed signing. Though, for EINVOICEs it will show up in the feed.
     */
    private PaymentPair fetchMatchingEInvoice(List<ProductEntity> paymentAccounts, Transfer transfer,
            Payment.StatusCode statusCode)
            throws IOException {
        List<PaymentPair> unsignedEInvoices = getPaymentsWithDetails(paymentAccounts, statusCode,
                Payment.SubType.EINVOICE);

        List<PaymentPair> matchingPayments = Lists.newArrayList();
        for (PaymentPair unsignedEInvoice : unsignedEInvoices) {
            PaymentDetailsResponseOut paymentDetails = unsignedEInvoice.getPaymentDetails();
            String fetchedTransferHash = paymentDetails.toEInvoiceTransfer().getHashIgnoreSource();

            if (Objects.equal(fetchedTransferHash, transfer.getHashIgnoreSource())) {
                matchingPayments.add(unsignedEInvoice);
            }
        }

        if (matchingPayments.isEmpty()) {
            this.log.info(transfer, String.format("No matching e-invoices! originalTransfer: %s, fetchedEInvoices: %s",
                    transfer, Iterables.toString(unsignedEInvoices)));

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            this.catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        } else if (!(matchingPayments.size() == 1)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            this.catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MULTIPLE_MATCHES))
                    .build();
        }

        return matchingPayments.get(0);
    }

    private static Transfer getOriginalTransfer(Transfer transfer) {
        Optional<Transfer> originalTransfer = transfer.getOriginalTransfer();

        if (!originalTransfer.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No original transfer on payload to compare with.").build();
        }

        return originalTransfer.get();
    }

    private boolean isTransferModifyingEInvoice(PaymentDetailsResponseOut matchingEInvoice, Transfer transfer) {
        Transfer fetchedTransfer = matchingEInvoice.toEInvoiceTransfer();
        if (Objects.equal(fetchedTransfer.getHashIgnoreSource(), transfer.getHashIgnoreSource())) {
            return false;
        }

        if (!matchingEInvoice.isAllowedToModify()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            this.catalog
                                    .getString(TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_NOT_ALLOWED))
                    .build();
        }

        // Some fields we cannot modify, check those first
        if (!Objects.equal(transfer.getDestination(), fetchedTransfer.getDestination()) ||
                !Objects.equal(transfer.getSource(), fetchedTransfer.getSource())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(this.catalog.getString(
                            TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_SOURCE_OR_DESTINATION))
                    .build();
        }

        // Some fields depends on whether the entity allows change or not
        if (!matchingEInvoice.isAllowedToModifyMessage() &&
                !Objects.equal(transfer.getDestinationMessage(), fetchedTransfer.getDestinationMessage())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(this.catalog.getString(
                            TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_MESSAGE))
                    .build();
        } else if (!matchingEInvoice.isAllowedToModifyAmount() &&
                !Doubles.fuzzyEquals(transfer.getAmount().getValue(), fetchedTransfer.getAmount().getValue(), 0.0001)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(this.catalog.getString(
                            TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_AMOUNT))
                    .build();
        } else if (!matchingEInvoice.isAllowedToModifyDueDate() &&
                !Objects.equal(transfer.getDueDate(), fetchedTransfer.getDueDate())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(this.catalog.getString(
                            TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_DUEDATE))
                    .build();
        }

        return true;
    }

    private PaymentPair updateEInvoice(List<ProductEntity> paymentAccounts,
            PaymentDetailsResponseOut matchingEInvoice, Transfer transfer) throws IOException {
        ChangePaymentRequest changePaymentRequest = ChangePaymentRequest.
                copyFromPaymentDetails(paymentAccounts, matchingEInvoice);

        changePaymentRequest.setAmount(transfer.getAmount());
        changePaymentRequest.setDueDate(transfer.getDueDate());
        changePaymentRequest.setDestinationMessage(transfer.getDestinationMessage());

        String url = this.market.getBankingEndpoint() + "/Payments/" + matchingEInvoice.getPaymentId();
        ChangePaymentResponse response = createJsonPutRequest(url, changePaymentRequest, ChangePaymentResponse.class);

        BankingServiceResponse serviceResponse = response.getBankingServiceResponse();
        if (serviceResponse != null && serviceResponse.getErrorMessage() != null) {
            ErrorMessage errorMessage = serviceResponse.getErrorMessage();
            String errorCode = (String) errorMessage.getErrorCode().get("$");
            if (errorCode == null) {
                errorCode = "";
            }
            this.log.error(String.format("Error when updating e-invoice: %s", errorCode));
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(NordeaErrorUtils.getErrorMessage((String) errorCode))
                    .build();
        }

        // Since we now have updated the transfer it should be unsigned, and thus considered an einvoice (although a PAYMENT if it's an update)
        transfer.setType(TransferType.EINVOICE);

        // We should now have a match on the new transfer hash if everything went as expected (status UNCONFIRMED since
        // it has been changed)
        ImmutableList<ProductEntity> sourceAccount = getSourceAccount(paymentAccounts, matchingEInvoice);
        return fetchMatchingEInvoice(sourceAccount, transfer, Payment.StatusCode.UNCONFIRMED);
    }

    private ImmutableList<ProductEntity> getSourceAccount(List<ProductEntity> paymentAccounts,
            PaymentDetailsResponseOut matchingEInvoice) {
        Predicate<ProductEntity> sourceAccountFilter = NordeaAgentUtils
                .getAccountIdFilter(ImmutableSet.of(matchingEInvoice.getFromAccountId()));

        return FluentIterable.from(paymentAccounts)
                .filter(sourceAccountFilter)
                .toList();
    }

    private void executePayment(Transfer transfer) throws IOException, InterruptedException {
        InitialContextResponse contextResponse = getInitialContext();

        if (contextResponse == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not retrieve initial context.").build();
        }

        ProductEntity sourceAccount = validateSourceAccount(transfer, contextResponse, true);
        Optional<BeneficiaryEntity> destinationAccount = validateDestinationAccount(transfer, true);

        if (destinationAccount.isPresent()) {
            makePayment(transfer, sourceAccount, destinationAccount.get());
        } else {
            BeneficiaryEntity beneficiary = createDestinationPayee(transfer);
            makePayment(transfer, sourceAccount, beneficiary);
        }
    }

    private void executeBankTransfer(Transfer transfer) throws IOException, InterruptedException {
        InitialContextResponse contextResponse = getInitialContext();

        if (contextResponse == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not retrieve initial context.").build();
        }

        // Find source account.
        ProductEntity sourceAccount = validateSourceAccount(transfer, contextResponse, false);

        // Find destination account.

        Optional<ProductEntity> destinationInternalAccount = contextResponse.getData().getProducts().stream()
                .filter(p -> Filters.productWithAccountNumber(transfer.getDestination().getIdentifier
                        (DEFAULT_FORMATTER)).apply(p))
                .findFirst();

        if (destinationInternalAccount.isPresent()) {
            // Internal transfers can be executed directly and doesn't need signing.
            makeInternalBankTransfer(transfer, sourceAccount, destinationInternalAccount.get());
        } else {
            Optional<BeneficiaryEntity> destinationExternalAccount = validateDestinationAccount(transfer, false);

            if (!destinationExternalAccount.isPresent()) {
                destinationExternalAccount = createDestinationAccount(transfer.getDestination());
            }

            if (!destinationExternalAccount.isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(this.catalog.getString(
                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
                        .build();
            }

            makeExternalBankTransfer(transfer, sourceAccount, destinationExternalAccount.get());
        }
    }

    private BeneficiaryEntity createDestinationPayee(Transfer transfer) {
        BeneficiaryEntity beneficiary = new BeneficiaryEntity();

        beneficiary.setToAccountId(transfer.getDestination().getIdentifier(new DisplayAccountIdentifierFormatter()));
        beneficiary.setBeneficiaryBankId("");
        beneficiary.setBeneficiaryNickName("");

        return beneficiary;
    }

    private Optional<BeneficiaryEntity> createDestinationAccount(AccountIdentifier accountIdentifier) {

        BeneficiaryEntity destinationAccount = new BeneficiaryEntity();

        Optional<String> recipientName = accountIdentifier.getName().isPresent() ?
                accountIdentifier.getName() : requestRecipientNameSupplemental();

        if (!recipientName.isPresent()) {
            return Optional.empty();
        }

        destinationAccount.setBeneficiaryNickName(recipientName.get());
        destinationAccount.setToAccountId(accountIdentifier.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER));
        destinationAccount.setBeneficiaryBankId(NordeaAgentUtils.lookupBeneficiaryBankId(accountIdentifier));

        return Optional.of(destinationAccount);
    }

    private Optional<String> requestRecipientNameSupplemental() {
        Field nameField = new Field();

        nameField.setDescription("Mottagarnamn");
        nameField.setName("name");

        List<Field> fields = Lists.newArrayList(nameField);

        this.credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        this.credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = this.context.requestSupplementalInformation(this.credentials, true);

        this.log.info("Supplemental Information response is: " + supplementalInformation);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return Optional.empty();
        }

        Map<String, String> answers = SerializationUtils.deserializeFromString(supplementalInformation,
                MAP_TYPE_REFERENCE);

        String recipientName = answers.get("name");

        if (Strings.isNullOrEmpty(recipientName)) {
            return Optional.empty();
        }

        return Optional.of(recipientName);
    }

    private void makeInternalBankTransfer(Transfer transfer, ProductEntity source, ProductEntity destination)
            throws IOException {
        BankTransferRequest transferRequest = BankTransferRequest.createNonRecurringBankTransfer();
        transferRequest.setAmount(transfer.getAmount().getValue());
        transferRequest.setMessage(getInternalTransferMessage(transfer));
        transferRequest.setSource(source);
        transferRequest.setDestination(destination);

        String url = this.market.getBankingEndpoint() + "/Transfers";

        InternalBankTransferResponse transferResponse = createJsonRequest(url, transferRequest,
                InternalBankTransferResponse.class);

        if (!transferResponse.isTransferAccepted()) {
            String errorMessage = transferResponse.getErrorMessage().isPresent() ?
                    transferResponse.getErrorMessage().get() :
                    this.catalog.getString("Something went wrong.");
            throw TransferExecutionException.builder(transferResponse.getErrorStatus())
                    .setEndUserMessage(errorMessage).build();
        }
    }

    /**
     * Since Nordea only has one message field, we want to have their default formatting of transfer messages
     * when transferring between two Nordea accounts. How they set the default message is better, since they internally
     * have different values on the source and destination account, but for us using the API we can only set one
     * message. So default to empty string to let them decide message if our client hasn't set any message.
     * <p>
     * If the client has set the destination message we use it with the formatter (in order to get it cut off if it's
     * too long).
     */
    private String getInternalTransferMessage(Transfer transfer) {
        String transferMessage;
        if (!Strings.isNullOrEmpty(transfer.getDestinationMessage())) {
            transferMessage = this.transferMessageFormatter.getDestinationMessage(transfer, true);
        } else {
            transferMessage = "";
        }
        return transferMessage;
    }

    private void makeExternalBankTransfer(Transfer transfer, ProductEntity source, BeneficiaryEntity destination)
            throws IOException, InterruptedException {
        if (Objects.equal(this.market.getMarketCode(), "FI")) {
            List<PaymentEntity> unsignedPaymentsFI = getPayments(Payment.StatusCode.UNCONFIRMED);

            if (unsignedPaymentsFI.size() > 0) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(this.catalog.getString(
                                TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                        .build();
            }
        }

        // Only use the formatted destination message for external transfers since we need to limit it to that
        String formattedTransferMessage = this.transferMessageFormatter.getDestinationMessage(transfer, false);

        String url = this.market.getBankingEndpoint() + "/Payments";

        TransferRequest transferRequest = TransferRequest.createNonRecurringBankTransferRequest();
        transferRequest.setAmount(transfer.getAmount().getValue());
        transferRequest.setMessage(formattedTransferMessage);
        transferRequest.setSource(source);
        transferRequest.setDestination(destination);

        TransferResponse transferResponse = createJsonRequest(url, transferRequest, TransferResponse.class);
        Optional<String> error = transferResponse.getError();

        if (error.isPresent()) {
            String errorMessage = NordeaErrorUtils.getErrorMessage(error.get(),
                    this.catalog.getString("Payment was not accepted at Nordea"));

            throw TransferExecutionException.builder(NordeaErrorUtils.getTransferErrorStatus(error.get()))
                    .setMessage("Payment was not accepted at Nordea: " + errorMessage)
                    .setEndUserMessage(errorMessage).build();
        }

        switch (this.market.getMarketCode()) {
        case "SE":
            List<PaymentEntity> unsignedPaymentsSE = getPayments(source, Payment.StatusCode.UNCONFIRMED);

            Optional<PaymentEntity> exclusivePaymentToSignSE = NordeaTransferUtils
                    .getSingleMatchingPaymentEntity(
                            unsignedPaymentsSE, transferRequest, transferResponse, transfer.getDestination());

            if (!exclusivePaymentToSignSE.isPresent()) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(this.catalog.getString(
                                TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                        .setMessage("Could not find exclusive match on transfer to sign")
                        .build();
            }

            signPayment(exclusivePaymentToSignSE.get());
            break;
        case "FI":
            List<PaymentEntity> unsignedPaymentsFI = getPayments(Payment.StatusCode.UNCONFIRMED);

            // TODO: Make sure that we can safely do signing on one specific transfer for FI like above
            if (unsignedPaymentsFI.size() != 1) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(this.catalog.getString("Payment was not accepted at Nordea"))
                        .build();
            }

            signPayment(unsignedPaymentsFI.get(0));
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Market not implemented")
                    .build();
        }
    }

    private void makePayment(Transfer transfer, ProductEntity source, BeneficiaryEntity destination)
            throws IOException, InterruptedException {
        Preconditions.checkState(Objects.equal(this.market.getMarketCode(), "SE"),
                String.format("Payments not implemented for market(%s)", this.market.getMarketCode()));

        String url = this.market.getBankingEndpoint() + "/Payments";

        TransferRequest paymentRequest = TransferRequest.createNonRecurringPaymentRequest();
        paymentRequest.setAmount(transfer.getAmount().getValue());
        paymentRequest.setMessage(transfer.getDestinationMessage());
        paymentRequest.setSource(source);
        paymentRequest.setDestination(destination);
        paymentRequest.setDueDate(transfer.getDueDate());
        paymentRequest.setPaymentType(transfer.getDestination());

        TransferResponse transferResponse = createJsonRequest(url, paymentRequest, TransferResponse.class);

        if (transferResponse.getError().isPresent()) {
            String errorMessage = this.catalog.getString("Payment was not accepted at Nordea");

            Optional<String> errorCode = transferResponse.getBankingServiceResponse().getErrorCode();
            SignableOperationStatuses errorStatus = SignableOperationStatuses.FAILED;
            if (errorCode.isPresent()) {
                errorMessage = NordeaErrorUtils.getErrorMessage(errorCode.get());
                errorStatus = NordeaErrorUtils.getTransferErrorStatus(errorCode.get());
            }

            throw TransferExecutionException.builder(errorStatus)
                    .setMessage("Payment was not accepted at Nordea: " + errorMessage)
                    .setEndUserMessage(errorMessage).build();
        }

        List<PaymentEntity> unsignedPaymentsSE = getPayments(source, Payment.StatusCode.UNCONFIRMED);

        Optional<PaymentEntity> exclusivePaymentToSignSE = NordeaTransferUtils
                .getSingleMatchingPaymentEntity(
                        unsignedPaymentsSE, paymentRequest, transferResponse, transfer.getDestination());

        if (!exclusivePaymentToSignSE.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            this.catalog
                                    .getString(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                    .setMessage("Could not find exclusive match on transfer to sign")
                    .build();
        }

        signPayment(exclusivePaymentToSignSE.get());
    }

    private void deleteExternalPayment(String paymentId) {
        try {
            String url = this.market.getBankingEndpoint() + "/Payments/";

            // Collect payment details to get the id that we can use to delete the payment
            PaymentDetailsResponse detailsResponse = createJsonRequest(url + paymentId, PaymentDetailsResponse.class);

            String content = createClientRequest(url + detailsResponse.getPaymentId(), this.securityToken)
                    .type(MediaType.APPLICATION_JSON_TYPE).delete(String.class);

            DeletePaymentsResponse response = MAPPER.readValue(content, DeletePaymentsResponse.class);

            if (response.isPaymentDeleted()) {
                this.log.debug("Payment deleted");
            } else {
                this.log.error("Payment was not removed from Nordea");
            }
        } catch (Exception e) {
            this.log.error("Caught exception while trying to remove failed transfer/payment", e);
        }
    }

    private void signEInvoice(PaymentEntity eInvoiceToSign) throws IOException, InterruptedException {
        Preconditions.checkState(Objects.equal(this.market.getMarketCode(), "SE"),
                String.format("Signing e-invoices for market(%s) not implemented", this.market.getMarketCode()));

        signPaymentSE(eInvoiceToSign);
    }

    private void signPayment(PaymentEntity paymentToSign) throws IOException, InterruptedException {
        try {
            switch (this.market.getMarketCode()) {
            case "SE":
                signPaymentSE(paymentToSign);
                break;
            case "FI":
                signPaymentFI(paymentToSign);
                break;
            default:
                throw new IllegalStateException(String.format(
                        "Signing payment/transfer for market(%s) not implemented", this.market.getMarketCode()));
            }
        } catch (Exception e) {
            deleteExternalPayment(paymentToSign.getPaymentId());

            if (e instanceof ThreadInterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw e;
        }
    }

    private void signPaymentSE(PaymentEntity paymentToSign) throws IOException, InterruptedException {

        String orderRef = initMobileBankIdPayment(paymentToSign);

        this.credentials.setSupplementalInformation(null);
        this.credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        this.context.requestSupplementalInformation(this.credentials, false);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            // Check and see if payment has been signed or aborted
            BankIdConfirmationResponse status = getPaymentStatus(orderRef);

            if (status.isPaymentSigned()) {
                return;
            }

            Optional<String> errorCode = status.getErrorCode();

            if (errorCode.isPresent()) {
                SignableOperationStatuses confirmationStatus;
                String errorMessage;

                switch (errorCode.get()) {
                case "MBS9001":
                case "MBS0905":     // TIMEOUT
                    errorMessage = this.catalog.getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE);
                    confirmationStatus = SignableOperationStatuses.CANCELLED;
                    break;
                case "MBS0902":     // CANCELLED
                    errorMessage = this.catalog.getString(TransferExecutionException.EndUserMessage.BANKID_CANCELLED);
                    confirmationStatus = SignableOperationStatuses.CANCELLED;
                    break;
                default:            // FAILED
                    confirmationStatus = SignableOperationStatuses.FAILED;
                    errorMessage = NordeaErrorUtils.getErrorMessage(errorCode.get());
                    break;
                }

                String logMessage = MoreObjects.toStringHelper(NordeaErrorUtils.class)
                        .add("errorMessage", errorMessage)
                        .add("errorCode", errorCode)
                        .toString();

                throw TransferExecutionException.builder(confirmationStatus)
                        .setMessage(String.format("Error when signing payment: %s", logMessage))
                        .setEndUserMessage(errorMessage).build();
            }

            this.log.debug(this.catalog.getString("Waiting on Mobile Bank Id"));
            Thread.sleep(2000);
        }

        // Should never get here since our timeout threshold is longer than Nordeas
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage("No response from Mobile BankID")
                .setEndUserMessage(this.catalog.getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
                .build();
    }

    private void signPaymentFI(PaymentEntity paymentToSign) throws IOException {
        // Get the next security token from the user
        String identificationCode = requestIdentificationCode();

        if (Strings.isNullOrEmpty(identificationCode)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Did not receive a identification code")
                    .setEndUserMessage(this.catalog.getString("Did not receive a identification code")).build();
        }

        // Collect and update the security token
        StrongLoginRequest request = new StrongLoginRequest(this.credentials.getUsername(), identificationCode);

        StrongLoginResponse response = createJsonRequest(this.market.getAuthenticationEndPoint() + "/SecurityToken",
                request, StrongLoginResponse.class);

        if (response.isError()) {
            String errorMessage = NordeaErrorUtils.getErrorMessage(response.getErrorCode());
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Error when signing payment: " + errorMessage)
                    .setEndUserMessage(errorMessage).build();
        }

        this.securityToken = response.getStrongResponseIn().getAuthenticationToken().getToken();

        ChallengeResponse challenge = createJsonRequest(this.market.getAuthenticationEndPoint() + "/Challenge",
                ChallengeResponse.class);

        if (Strings.isNullOrEmpty(challenge.getChallengeResponseIn().getChallengeCharacter())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED).build();
        }

        String confirmationCode = requestVerificationCode(challenge.getChallengeResponseIn().getChallengeCharacter());

        ConfirmPaymentsRequest confirmPaymentsRequest = new ConfirmPaymentsRequest(paymentToSign.getPaymentId(),
                confirmationCode, challenge.getChallengeResponseIn().getId());

        ConfirmPaymentsResponse confirmPaymentsResponse = createJsonPutRequest(
                this.market.getBankingEndpoint() + "/Payments?type=confirm", confirmPaymentsRequest,
                ConfirmPaymentsResponse.class);

        if (confirmPaymentsResponse.isPaymentSigned()) {
            return;
        } else if (confirmPaymentsResponse.isError()) {
            String errorMessage = NordeaErrorUtils.getErrorMessage(confirmPaymentsResponse.getErrorCode());
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Error when confirming payment: " + errorMessage)
                    .setEndUserMessage(errorMessage).build();
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Payment was not signed")
                    .setEndUserMessage(this.catalog.getString("Payment was not signed")).build();
        }
    }

    private Optional<BeneficiaryEntity> validateDestinationAccount(Transfer transfer, boolean isPayment)
            throws IOException {
        Stream<BeneficiaryEntity> beneficiaries = getBeneficiaries().stream();

        if (isPayment) {
            return beneficiaries.filter(b ->
                    Filters.beneficiariesWithAccountNumber(transfer.getDestination().getIdentifier(
                            DEFAULT_FORMATTER)).apply(b)).findFirst();
        } else {
            return beneficiaries.filter(b ->
                    Filters.beneficiariesWithAccountNumber(transfer.getDestination().getIdentifier(
                            ACCOUNT_IDENTIFIER_FORMATTER)).apply(b)).findFirst();
        }
    }

    private ProductEntity validateSourceAccount(Transfer transfer, InitialContextResponse contextResponse,
            boolean isPayment) {
        Stream<ProductEntity> internalAccounts = contextResponse.getData().getProducts().stream();

        Optional<ProductEntity> sourceAccount;
        if (isPayment) {
            sourceAccount = internalAccounts.filter(a ->
                    Filters.productThatCanPayWithAccountNumber(transfer.getSource().getIdentifier(DEFAULT_FORMATTER))
                            .apply(a))
                    .findFirst();
        } else {
            sourceAccount = internalAccounts.filter(a ->
                    Filters.productWithAccountNumber(transfer.getSource().getIdentifier(DEFAULT_FORMATTER)).apply(a))
                    .findFirst();
        }

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(this.catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                    .build();
        }
        return sourceAccount.get();
    }

    private String requestIdentificationCode() throws IOException {

        Field responseField = new Field();

        responseField.setDescription(this.catalog.getString("Next unused code"));
        responseField.setName("response");
        responseField.setNumeric(true);
        responseField.setHint("NNNN");
        responseField.setMaxLength(4);
        responseField.setMinLength(4);
        responseField.setMasked(true);
        responseField.setPattern("([0-9]{4})");

        List<Field> fields = Lists.newArrayList(responseField);

        this.credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        this.credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = this.context.requestSupplementalInformation(this.credentials, true);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers = MAPPER.readValue(supplementalInformation, MAP_TYPE_REFERENCE);

        return answers.get("response");
    }

    private String requestVerificationCode(String challengeCharacter) throws IOException {

        Field responseField = new Field();

        responseField.setDescription("Kod:" + challengeCharacter);
        responseField.setName("response");
        responseField.setNumeric(true);
        responseField.setHint("NNNN");
        responseField.setMaxLength(4);
        responseField.setMinLength(4);
        responseField.setPattern("([0-9]{4})");

        List<Field> fields = Lists.newArrayList(responseField);

        this.credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        this.credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = this.context.requestSupplementalInformation(this.credentials, true);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers = MAPPER.readValue(supplementalInformation, MAP_TYPE_REFERENCE);

        return answers.get("response");
    }

    private List<PaymentPair> getPaymentsWithDetails(ProductEntity account, Payment.StatusCode status)
            throws IOException {
        return getPaymentsWithDetails(Lists.newArrayList(account), status);
    }

    private List<PaymentPair> getPaymentsWithDetails(List<ProductEntity> accounts, Payment.StatusCode status)
            throws IOException {
        return getPaymentsWithDetails(accounts, status, null);
    }

    private List<PaymentPair> getPaymentsWithDetails(List<ProductEntity> accounts, Payment.StatusCode status,
            Payment.SubType type)
            throws IOException {
        List<PaymentPair> paymentPairs = Lists.newArrayList();

        // We get payments for each account separately, because we've encountered problems when requesting payments for
        // multiple accounts at the same time.
        for (ProductEntity account : Optional.ofNullable(accounts).orElse(Collections.emptyList())) {
            for (PaymentEntity paymentEntity : getPayments(account, status)) {
                PaymentDetailsResponseOut paymentDetails = getPaymentDetails(paymentEntity);

                // When Nordea has temp errors we get null in payment details, so we need to null check it to avoid NPE's
                if (paymentDetails == null) {
                    this.log.warn("Unexpected paymentDetails == null. Continuing to avoid NPE.");
                    continue;
                }

                // Don't include if type is a mismatch and type is specified (we cannot do this directly on the PaymentEntity since it doesn't always include accurate type, e.g. EINVOICE type)
                if (type != null && !Objects.equal(paymentDetails.getPaymentSubType(), type)) {
                    continue;
                }

                paymentPairs.add(new PaymentPair(paymentEntity, paymentDetails));
            }
        }

        return paymentPairs;
    }

    private List<PaymentEntity> getPayments(Payment.StatusCode status) throws IOException {
        return getPayments(Lists.<ProductEntity>newArrayList(), status);
    }

    private List<PaymentEntity> getPayments(ProductEntity account, Payment.StatusCode status) throws IOException {
        return getPayments(Lists.newArrayList(account), status);
    }

    private List<PaymentEntity> getPayments(List<ProductEntity> accounts, Payment.StatusCode status) {
        ImmutableSet<String> accountIds = null;
        if (accounts != null && !accounts.isEmpty()) {
            accountIds = getAccountIds(accounts);
            if (accountIds.isEmpty()) {
                return Lists.newArrayList();
            }
        }

        String url = getPaymentListUrl(accountIds, status);
        List<PaymentEntity> payments;

        try {
            WebResource.Builder request = createClientRequest(url, this.securityToken);

            switch (status) {
            case UNCONFIRMED:
                UnconfirmedPaymentListResponse unconfirmedResponse = request.get(UnconfirmedPaymentListResponse.class);
                payments = unconfirmedResponse.getPayments(Payment.StatusCode.UNCONFIRMED);
                break;
            case CONFIRMED:
                PaymentListResponse confirmedResponse = request.get(PaymentListResponse.class);
                payments = confirmedResponse.getPayments(Payment.StatusCode.CONFIRMED);
                break;
            default:
                throw new IllegalStateException("Unexpected status");
            }
        } catch (UniformInterfaceException e) {
            if (Objects.equal(e.getResponse().getStatus(), 204)) {
                this.log.debug("No payments available");
                return Lists.newArrayList();
            }

            throw e;
        }

        if (accountIds == null) {
            return payments;
        }

        // Nordea returns also what they call "NotifiedPayments" that is not assigned to any account, so we should not include these in the upcoming if we want to filter on specific accounts
        return Lists.newArrayList(FluentIterable
                .from(payments)
                .filter(NordeaAgentUtils.getPaymentFilter(accounts)));
    }

    private String getPaymentListUrl(Set<String> accountIds, Payment.StatusCode status) {
        StringBuilder paymentUrl = new StringBuilder(this.market.getBankingEndpoint() + "/Payments");
        switch (status) {
        case UNCONFIRMED:
            paymentUrl.append("?type=unconfirmed");
            break;
        case CONFIRMED:
            paymentUrl.append("?type=list");
            break;
        default:
            throw new IllegalArgumentException("Unexpected status");
        }

        if (accountIds == null || accountIds.isEmpty()) {
            return paymentUrl.toString();
        }

        return paymentUrl
                .append("&accountIds=").append(Joiner.on(";").join(accountIds))
                .toString();
    }

    private ImmutableSet<String> getAccountIds(List<ProductEntity> productEntities) {
        return getAccountIds(productEntities, null);
    }

    private ImmutableSet<String> getAccountIds(List<ProductEntity> productEntities, Predicate<ProductEntity> filter) {
        FluentIterable<ProductEntity> entityIterable = FluentIterable.from(productEntities);

        if (filter != null) {
            entityIterable = entityIterable.filter(filter);
        }

        return entityIterable
                .transform(NordeaAgentUtils.PRODUCT_TO_INTERNAL_ID)
                .toSet();
    }

    private PaymentDetailsResponseOut getPaymentDetails(PaymentEntity paymentEntity) throws IOException {
        Preconditions.checkNotNull(paymentEntity);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(paymentEntity.getPaymentId()));

        String url = this.market.getBankingEndpoint() + "/Payments/" + paymentEntity.getPaymentId();
        String content = createClientRequest(url, this.securityToken).get(String.class);

        return MAPPER.readValue(content, PaymentDetailsResponse.class).getPaymentDetailsResponseOut();
    }

    private String initMobileBankIdPayment(PaymentEntity entity) throws IOException {
        String url = this.market.getBankingEndpoint() + "/MobileBankIdInitialConfirmation";

        InitBankIdPaymentRequest request = new InitBankIdPaymentRequest(entity);

        Optional<String> orderRef = createJsonRequest(url, request, InitBankIdConfirmPaymentResponse.class)
                .getOrderRef();

        if (!orderRef.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(this.catalog.getString(TransferExecutionException.EndUserMessage.BANKID_FAILED))
                    .setMessage("Failed to initiate BankID signing ( No reference returned by Nordea )")
                    .build();
        }

        return orderRef.get();
    }

    private BankIdConfirmationResponse getPaymentStatus(String orderRef) throws IOException {
        String url = this.market.getBankingEndpoint() + "/MobileBankIdConfirmation";

        BankIdConfirmationRequest request = new BankIdConfirmationRequest(orderRef);

        return createJsonRequest(url, request, BankIdConfirmationResponse.class);
    }

    private <T> T createJsonRequest(String url, Object input, Class<T> returnType) throws IOException {
        String payload = MAPPER.writeValueAsString(input);

        String content = createClientRequest(url, this.securityToken)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, payload);

        return MAPPER.readValue(content, returnType);
    }

    private <T> T createJsonPutRequest(String url, Object input, Class<T> returnType) throws IOException {
        String payload = MAPPER.writeValueAsString(input);

        String content = createClientRequest(url, this.securityToken)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(String.class, payload);

        return MAPPER.readValue(content, returnType);
    }

    private <T> T createJsonRequest(String url, Class<T> type) throws IOException {
        String content = createClientRequest(url, this.securityToken)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        return MAPPER.readValue(content, type);
    }

    /**
     * User is logged in if we can retrieve the context without any error.
     */
    @Override
    public boolean isLoggedIn() {
        if (this.securityToken == null) {
            return false;
        }

        try {

            String url = this.market.getBankingEndpoint() + "/initialContext";

            InitialContextResponse contextResponse = createJsonRequest(url, InitialContextResponse.class);

            if (contextResponse == null) {
                this.log.info("Could not retrieve context");
                return false;
            }

            BankingServiceResponse errorResponse = contextResponse.getBankingServiceResponse();

            if (errorResponse != null) {
                this.log.info("Session is not alive.");
                return false;
            }

            this.log.info("Session is still alive");
            return true;

        } catch (Exception ex) {
            this.log.error("Exception during context call", ex);
            return false;
        }
    }

    /**
     * The session is valid for 30 minutes and the token is valid for 10 minutes. This means that the session must be
     * renewed at least every 10th minute to keep the user logged in for a maximum on 30 minutes.
     */
    @Override
    public boolean keepAlive() throws Exception {
        if (this.securityToken == null) {
            return false;
        }

        String url = this.market.getAuthenticationEndPoint() + "/RenewSecurityToken";

        ClientResponse clientResponse = createClientRequest(url, this.securityToken)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        if (clientResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            this.log.warn("Security Token could not be renewed. Got status " + clientResponse.getStatus());
            this.securityToken = null;
            return false;
        }

        RenewSecurityTokenResponse tokenResponse = clientResponse.getEntity(RenewSecurityTokenResponse.class);
        AuthenticationToken token = tokenResponse.getSecurityToken().getAuthenticationToken();

        if (token == null) {
            this.log.warn("Security Token could not be renewed");
            this.securityToken = null;
            return false;
        }

        // Update the security token with the renewed one
        this.securityToken = token.getToken();
        this.securityTokenLastUpdated = DateTime.now();

        String message = String.format("Security Token Renewed Until: %s Login Time: %s", token.getNotAfter(),
                token.getLoginTime());

        this.log.info(message);

        return true;
    }

    @Override
    public void persistLoginSession() {
        Session session = new Session();
        session.setSecurityToken(this.securityToken);
        session.setLastUpdated(this.securityTokenLastUpdated);

        this.credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {

        Session session = this.credentials.getPersistentSession(Session.class);

        if (session == null) {
            return;
        }

        if (session.isExpired()) {
            return;
        }

        this.securityToken = session.getSecurityToken();
    }

    @Override
    public void clearLoginSession() {
        this.securityToken = null;

        this.credentials.removePersistentSession();
    }

    /**
     * Pair of entity model and details model used for response object in eInvoice signing
     * <p>
     * Reason: DetailsResponseOut doesn't have `paymentType`, and PaymentEntity doesn't have `messageRow` and correct
     * `paymentSubType`
     */
    private static class PaymentPair {
        private static final Function<PaymentPair, PaymentDetailsResponseOut> TO_PAYMENTDETAILS =
                new Function<PaymentPair, PaymentDetailsResponseOut>() {
                    @Nullable
                    @Override
                    public PaymentDetailsResponseOut apply(@Nullable PaymentPair paymentPair) {
                        if (paymentPair == null) {
                            return null;
                        }

                        return paymentPair.getPaymentDetails();
                    }
                };

        private final PaymentEntity paymentEntity;
        private final PaymentDetailsResponseOut paymentDetailsResponseOut;

        private PaymentPair(
                PaymentEntity paymentEntity, PaymentDetailsResponseOut paymentDetailsResponseOut) {
            this.paymentEntity = paymentEntity;
            this.paymentDetailsResponseOut = paymentDetailsResponseOut;
        }

        private PaymentEntity getPaymentEntity() {
            return this.paymentEntity;
        }

        private PaymentDetailsResponseOut getPaymentDetails() {
            return this.paymentDetailsResponseOut;
        }

        private Transaction toTransaction(Optional<Transfer> modifiableTransfer) throws JsonProcessingException {
            Transaction transaction = this.paymentEntity.toTransaction();
            if (modifiableTransfer.isPresent()) {
                Transfer transfer = modifiableTransfer.get();
                transfer.setId(UUIDUtils.fromTinkUUID(transaction.getId()));

                transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                        MAPPER.writeValueAsString(transfer));
                transaction.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                        UUIDUtils.toTinkUUID(transfer.getId()));
            }
            return transaction;
        }

        /**
         * We can only change einvoices on Nordea (since else we lose track of a payment if signing fails)
         */
        private Optional<Transfer> toModifiableTransfer() {
            if (this.paymentDetailsResponseOut != null &&
                    Objects.equal(this.paymentDetailsResponseOut.getPaymentSubType(), Payment.SubType.EINVOICE)) {
                return Optional.of(this.paymentDetailsResponseOut.toEInvoiceTransfer());
            }

            return Optional.empty();
        }

        @Override
        public String toString() {
            return getPaymentDetails().toEInvoiceTransfer().toString();
        }
    }
}
