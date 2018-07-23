package se.tink.backend.aggregation.agents.banks.swedbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.swedbank.model.AbstractResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.AmountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.BankEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.BaseInfoPaymentResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.CardAccountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ConfirmedInvoice;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ConfirmedPaymentResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.CreateRecipientResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.EndowmentInsuranceEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.EndowmentInsuranceHoldingEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.EndowmentInsurancePlacementEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.EndowmentInsuranceSubPlacementEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ErrorMessage;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ErrorMessages;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ErrorResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.FundHoldingsEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.HoldingsEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.HoldingsResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.Invoice;
import se.tink.backend.aggregation.agents.banks.swedbank.model.InvoiceDetails;
import se.tink.backend.aggregation.agents.banks.swedbank.model.InvoiceDocument;
import se.tink.backend.aggregation.agents.banks.swedbank.model.InvoicesResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.IskHoldingEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.IskPlacementEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.IskSubPlacementEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.LinkEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.LoanResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MarketInfoEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MenuItem;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MobileBankIdRequest;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MobileBankIdResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MobileBankIdSignRequest;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MobileBankIdSignResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.MobileBankIdVerifyResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.Payee;
import se.tink.backend.aggregation.agents.banks.swedbank.model.PaymentAccountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.PaymentReference;
import se.tink.backend.aggregation.agents.banks.swedbank.model.PaymentRequest;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ProfileMenu;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ProfileResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.ProfilesHandler;
import se.tink.backend.aggregation.agents.banks.swedbank.model.RecipientEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.RegisteredPaymentResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.RemindersResponse;
import se.tink.backend.aggregation.agents.banks.swedbank.model.Session;
import se.tink.backend.aggregation.agents.banks.swedbank.model.SettlementEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.SwedbankAccountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.SwedbankAccountEntity.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.banks.swedbank.model.SwedbankTransferResponseFilter;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransactionAccountEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransactionAccountGroupEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransferRequest;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransferTransactionDetails;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransferTransactionEntity;
import se.tink.backend.aggregation.agents.banks.swedbank.model.TransferTransactionGroupEntity;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.log.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.i18n.SocialSecurityNumber.Sweden;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankAgent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor,
        PersistentLogin {
    private static class ProfileParameters {
        private final String apiKey;
        private final String name;
        private final boolean savingsBank;

        private ProfileParameters(String name, String apiKey, boolean savingsBank) {
            this.name = name;
            this.apiKey = apiKey;
            this.savingsBank = savingsBank;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getName() {
            return name;
        }

        private boolean isSavingsBank() {
            return savingsBank;
        }
    }

    private static final Pattern ALL_ALLOWED_ACCOUNT_BANKIDS;
    private static final Pattern ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN;
    private static final Pattern ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN;
    private static final String BASE_URL = "https://auth.api.swedbank.se/TDE_DAP_Portal_REST_WEB/api";
    private static final String BASE_URL_V5 = BASE_URL + "/v5";
    private static final TypeReference<HashMap<String, String>> FIELDS_TYPE_REFERENCE = new TypeReference<HashMap<String, String>>() {
    };
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ImmutableMap<String, ProfileParameters> PROFILE_PARAMETERS =
            new ImmutableMap.Builder<String, ProfileParameters>()
                    .put("swedbank",
                            new ProfileParameters("swedbank", "rMKD7LKhhFNVOXJK", false))
                    .put("swedbank-youth",
                            new ProfileParameters("swedbank-youth", "ap4TcWEoEGV42UVn", false))
                    .put("savingsbank",
                            new ProfileParameters("savingsbank", "CB2PGrGdDIJKcrRd", true))
                    .put("savingsbank-youth",
                            new ProfileParameters("savingsbank-youth", "LFQP9KuzqNBJOosw", true))
                    .build();

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SAVINGS_GOAL_NAME = "Tink";

    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();
    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig
            .createWithMaxLength(10);

    static {
        final Joiner REGEXP_OR_JOINER = Joiner.on("|");
        final ImmutableSet<String> ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS = ImmutableSet.of(
                "\\*{4} \\*{4} \\*{4} ([0-9]{4}|\\*{4})",
                "\\*{4} \\*{6} [0-9]{5}",
                "\\*{4} \\*{6} \\*{5}",
                "[0-9]{4}-[0-9],[0-9]{1,3} [0-9]{3} [0-9]{3}-[0-9]",
                "[0-9]{4}-[0-9]{2}-[0-9]{5}",
                "[0-9]{4} [0-9\\*]{4} [0-9\\*]{4} [0-9]{4}",
                "[0-9]{4} [0-9]{6} [0-9]{5}",
                Pattern.quote("**** **** **** **"));
        final ImmutableSet<String> ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS = ImmutableSet.of(
                "([0-9]{4}-[0-9],)?[1-9][0-9]{0,2}( [0-9]{3})*-[0-9] ?",
                "[0-9]{9,11}",
                "[0-9]{4}-[0-9]{2}-[0-9]{5} ?",
                "\\*{4}( \\*{4}){2} (\\*{4}|[0-9]{4})",
                "[0-9]{21}",
                "[0-9]{15,16}",
                "[0-9]{10}A",
                "[0-9]{5}LI-[0-9]{7}-[0-9]{3}");
        ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN = Pattern.compile(REGEXP_OR_JOINER
                .join(ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS));
        ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN = Pattern.compile(REGEXP_OR_JOINER
                .join(ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS));
        ALL_ALLOWED_ACCOUNT_BANKIDS = Pattern.compile(REGEXP_OR_JOINER.join(Sets.union(
                ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS, ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS)));
    }

    private static List<Transaction> convertTransactions(List<TransactionEntity> transactionEntities) {
        ArrayList<Transaction> transactions = Lists.newArrayList();

        for (TransactionEntity transactionEntity : transactionEntities) {
            Transaction transaction = new Transaction();

            transaction.setDate(parseDate(transactionEntity.getDate(), true));
            transaction.setDescription(SwedbankAgentUtils.cleanDescription(cleanDescription(transactionEntity
                    .getDescription())));

            if (transactionEntity.getLocalAmount() != null) {
                transaction.setAmount(parseAmount(transactionEntity.getLocalAmount().getAmount()));
            } else {
                transaction.setAmount(parseAmount(transactionEntity.getAmount()));
            }

            // Update transaction to pending if the description is "Övf via internet". This is because
            // Swedbank later will change description and include the account number like
            // "Övf via internet [Account Number]"

            if (transaction.getDescription()
                    .equalsIgnoreCase("ÖVF VIA INTERNET")) {
                transaction.setPending(true);
            }

            transactions.add(transaction);
        }

        return transactions;
    }

    private static String generateDSID() {
        byte bytes[] = new byte[6];

        Base64 base64 = new Base64(100, null, true);

        RANDOM.nextBytes(bytes);

        return base64.encodeAsString(bytes);
    }

    private final TransferMessageFormatter transferMessageFormatter;
    private ApacheHttpClient4 client;
    private Credentials credentials;
    private ProfileParameters profileParameters;
    private Map<String, List<Transaction>> upcomingTransactionsByBankId;
    private static final int MAX_ATTEMPTS = 90;
    private static final int MAX_RETRY_ATTEMPTS = 4;
    private ProfilesHandler profilesHandler = new ProfilesHandler();

    public SwedbankAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);

        credentials = request.getCredentials();
        profileParameters = PROFILE_PARAMETERS.get(request.getProvider().getPayload());

        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();
        clientConfig.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);
        client = clientFactory.createCookieClient(context.getLogOutputStream(), clientConfig);

        transferMessageFormatter = new TransferMessageFormatter(context.getCatalog(),
                TRANSFER_MESSAGE_LENGTH_CONFIG,
                new StringNormalizerSwedish("-?!/+:"));
    }

    private boolean authenticateWithMobiltBankId() throws AuthenticationException {

        MobileBankIdRequest mobileBankIdRequest = new MobileBankIdRequest();

        mobileBankIdRequest.setUserId(credentials.getField(Field.Key.USERNAME));
        mobileBankIdRequest.setUseEasyLogin(false);
        mobileBankIdRequest.setGenerateEasyLoginId(true);

        // Initiate the BankID connection.

        ClientResponse response = createClientRequest(
                BASE_URL_V5 + "/identification/bankid/mobile", true).post(ClientResponse.class, mobileBankIdRequest);

        if (response.getStatus() == 400) {
            AbstractResponse error = response.getEntity(AbstractResponse.class);
            ErrorMessages errorMessages = error.getErrorMessages();
            if (errorMessages != null && !errorMessages.getFields().isEmpty()) {

                // check if the user entered an invalid SSN
                for (ErrorMessage field : errorMessages.getFields()) {
                    String message = field.getMessage();
                    // search for: "Du har angett ett felaktigt personnummer. Vänligen försök igen."
                    if (message.toLowerCase().contains("felaktigt personnummer")) {
                        throw LoginError.INCORRECT_CREDENTIALS.exception();
                    }
                }

                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Login Failed with error message: %s",
                                errorMessages));
            }
        }

        MobileBankIdResponse mobileBankIdResponse = response.getEntity(MobileBankIdResponse.class);

        if (Objects.equal(mobileBankIdResponse.getStatus(), "OUTSTANDING_TRANSACTION")) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        }

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        context.requestSupplementalInformation(credentials, false);

        // Verify the result of the BankID authentication.
        String previousStatus = "";
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            MobileBankIdVerifyResponse mobileBankIdVerifyResponse = verifyBankIdStatus(previousStatus, mobileBankIdResponse);

            if (Objects.equal(mobileBankIdVerifyResponse.getStatus(), "COMPLETE")) {
                return true;
            }

            // The BankID authentication was cancelled or timed out.
            if (Objects.equal(mobileBankIdVerifyResponse.getStatus(), "CANCELLED")) {
                throw BankIdError.CANCELLED.exception();
            } else if (mobileBankIdVerifyResponse.hasGeneralErrorWithCode("LOGIN_FAILED")) {
                throw BankIdError.NO_CLIENT.exception();
            } else if (mobileBankIdVerifyResponse.hasGeneralErrorWithCode("INTERNAL_SERVER_ERROR") &&
                    Objects.equal(previousStatus.toLowerCase(), "client_not_started")) {
                // This code is a temporary fix until Swedbank returns a better error message.
                // What we belive to be the problem is that when multiple request are sent to bankid at the same time
                // bankid cancels all requests.
                throw BankIdError.CANCELLED.exception();
            }

            log.info("Waiting for BankID completion, status " + mobileBankIdVerifyResponse.getStatus());
            previousStatus = mobileBankIdVerifyResponse.getStatus();
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        // User failed to authenticate using BankID in time.

        throw BankIdError.TIMEOUT.exception();
    }

    private MobileBankIdVerifyResponse verifyBankIdStatus(String previousStatus, MobileBankIdResponse mobileBankIdResponse) {
        try {
            return createClientRequest(BASE_URL + mobileBankIdResponse.getLinks().getNext().getUri(), false)
                    .get(MobileBankIdVerifyResponse.class);
        } catch (UniformInterfaceException e) {
            ClientResponse clientResponse = e.getResponse();

            //  401 responses are useful responses, including the error status (e.g. on a timeout error)
            if (clientResponse.getStatus() == 401) {
                return clientResponse.getEntity(MobileBankIdVerifyResponse.class);
            } else if (clientResponse.getStatus() == 500) {
                // This code is a temporary fix until Swedbank returns a better error message.
                // What we belive to be the problem is that when multiple request are sent to bankid at the same time
                // bankid cancels all requests.
                if (previousStatus != null && Objects.equal(previousStatus.toLowerCase(), "client_not_started")) {
                    return clientResponse.getEntity(MobileBankIdVerifyResponse.class);
                } else {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            } else {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    /**
     * To be able to separate out exceptions from executing a transfer.
     *
     * @throws TransferExecutionException if status != 2xx
     */
    private <T> T createTransferRequest(RequestMethod requestMethod, String url, Class<T> responseType)
            throws Exception {
        return createTransferRequest(requestMethod, url, null, responseType);
    }

    private <T> T createTransferRequest(RequestMethod requestMethod, String url, Object request, Class<T> responseType)
            throws Exception {

        T response = null;
        Builder clientRequest = createClientRequest(url, true);
        switch (requestMethod) {
        case GET:
            response = clientRequest.get(responseType);
            break;
        case POST:
            if (request == null) {
                response = clientRequest.post(responseType);
            } else {
                response = clientRequest.post(responseType, request);
            }
            break;
        case PUT:
            if (request == null) {
                response = clientRequest.put(responseType);
            } else {
                response = clientRequest.put(responseType, request);
            }
            break;
        }

        return response;
    }

    private <T> T sendRetryableGetRequest(final String url, final Class<T> responseEntity) {
        return sendRetryableGetRequest(url, responseEntity, 0);
    }

    private <T> T sendRetryableGetRequest(String url, Class<T> responseEntity, int attempt) {
        Uninterruptibles.sleepUninterruptibly(attempt * 2, TimeUnit.SECONDS);

        try {
            return createClientRequest(url, false).get(responseEntity);
        } catch (UniformInterfaceException e) {
            if (Objects.equal(e.getResponse().getStatus(), 500) && attempt < MAX_RETRY_ATTEMPTS) {
                attempt += 1;
                log.warn(String.format("API call (url: %s) failed, retrying (attempt: %s)", url, attempt));
                return sendRetryableGetRequest(url, responseEntity, attempt);
            }

            throw e;
        }
    }

    /**
     * Helper method to create a client request.
     */
    private Builder createClientRequest(String url, boolean isPost) {
        String dsid = generateDSID();
        String authorization = generateAuthorization();

        String uri = UriBuilder.fromUri(url).queryParam("dsid", dsid).build().toString();

        Builder request = client.resource(uri).header("User-Agent", DEFAULT_USER_AGENT)
                .cookie(new NewCookie("dsid", dsid)).accept("*/*").acceptLanguage("sv-se")
                .header("Authorization", authorization);

        if (isPost) {
            request = request.type(MediaType.APPLICATION_JSON);
        }

        return request;
    }

    /**
     * Creates a new recipient for the transfer destination account.
     */
    private SwedbankAccountEntity createSignedRecipient(ProfileMenu profileMenu, final Transfer transfer) throws Exception {
        MenuItem menuItem = profileMenu.getMenuItem("PaymentRegisterExternalRecipient");

        if (!menuItem.isAuthorizedURI(MenuItem.Method.POST)) {
            throw BankIdError.AUTHORIZATION_REQUIRED.exception(UserMessage.STRONGER_AUTHENTICATION_NEEDED.getKey());
        }

        AccountIdentifier accountIdentifier = transfer.getDestination();
        if (accountIdentifier.getType() != AccountIdentifier.Type.SE) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED).setEndUserMessage(
                    context.getCatalog().getString("You can only make transfers to Swedish accounts")).build();
        }
        SwedishIdentifier destination = accountIdentifier.to(SwedishIdentifier.class);

        String recipientName = getDestinationName(transfer);

        // Get the new recipient
        RecipientEntity recipientEntity = createRecipient(destination, recipientName);

        CreateRecipientResponse response = createTransferRequest(
                RequestMethod.POST,
                BASE_URL + menuItem.getUri(),
                recipientEntity,
                CreateRecipientResponse.class);

        return signAndConfirmNewRecipient(response, findNewRecipientFromPaymentResponse(recipientEntity));
    }

    private RecipientEntity createRecipient(SwedishIdentifier destination, String recipientName) {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setType("BANKACCOUNT");
        recipientEntity.setRecipientNumber(getCleanRecipientNumber(destination));
        recipientEntity.setName(recipientName);

        return recipientEntity;
    }

    private String getCleanRecipientNumber(SwedishIdentifier destination) {
        StringBuilder cleanRecipientNumber = new StringBuilder();
        cleanRecipientNumber.append(destination.getClearingNumber());
        cleanRecipientNumber.append(destination.getAccountNumber());
        return cleanRecipientNumber.toString().replaceAll("[^0-9]", "");
    }

    /**
     * Returns a function that streams through all registered recipients with a filter to find the newly added recipient
     * among them.
     */
    private Function<BaseInfoPaymentResponse, Optional<SwedbankAccountEntity>> findNewRecipientFromPaymentResponse(
            RecipientEntity newRecipientEntity) {

        return confirmResponse -> confirmResponse.getAllRecipientAccounts().stream()
                        .filter(account -> account.getFullyFormattedNumber()
                                .replaceAll("[^0-9]", "")
                                .equals(newRecipientEntity.getRecipientNumber()))
                        .findFirst()
                        .map(SwedbankAccountEntity.class::cast);
    }

    /**
     * Creates a new payee.
     */
    private SwedbankAccountEntity createSignedPayee(ProfileMenu profileMenu, final Transfer transfer) throws Exception {
        MenuItem menuItem = profileMenu.getMenuItem("PaymentRegisterPayee");

        if (!menuItem.isAuthorizedURI(MenuItem.Method.POST)) {
            throw BankIdError.AUTHORIZATION_REQUIRED.exception(UserMessage.STRONGER_AUTHENTICATION_NEEDED.getKey());
        }

        String recipientName = getDestinationName(transfer);

        AccountIdentifier accountIdentifier = transfer.getDestination();
        if (!accountIdentifier.is(Type.SE_PG) && !accountIdentifier.is(Type.SE_BG)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(
                            context.getCatalog().getString("You can only make payments to Swedish destinations"))
                    .build();
        }

        // Create the new payee
        Payee payee = createPayee(accountIdentifier, recipientName);

        CreateRecipientResponse response = createTransferRequest(
                RequestMethod.POST,
                BASE_URL + menuItem.getUri(),
                payee,
                CreateRecipientResponse.class);

        return signAndConfirmNewRecipient(response, findNewPayeeFromPaymentResponse(payee));
    }

    private Payee createPayee(AccountIdentifier accountIdentifier, String recipientName) {
        Payee payee = new Payee();
        payee.setAccountNumber(accountIdentifier.getIdentifier(DEFAULT_FORMATTER));
        payee.setName(recipientName);
        payee.setType(accountIdentifier.is(Type.SE_BG) ? SwedbankPaymentType.BGACCOUNT.name()
                : SwedbankPaymentType.PGACCOUNT.name());

        return payee;
    }

    /**
     * Returns a function that streams through all registered payees with a filter to find the newly added payee
     * among them.
     */
    private Function<BaseInfoPaymentResponse, Optional<SwedbankAccountEntity>> findNewPayeeFromPaymentResponse(
            Payee newPayee) {
        String newPayeeType = newPayee.getType().toLowerCase();
        String newPayeeAccountNumber = newPayee.getAccountNumber().replaceAll("[^0-9]", "");

        return confirmResponse -> confirmResponse.getPayment().getPayees().stream()
                .filter(payee -> payee.getType().toLowerCase().equals(newPayeeType)
                        && payee.getAccountNumber().replaceAll("[^0-9]", "").equals(newPayeeAccountNumber))
                .findFirst()
                .map(SwedbankAccountEntity.class::cast);
    }

    /**
     * Signs and confirms the creation of a new recipient or payee. Returns the created recipient or throws an exception
     * if the creation failed.
     */
    private SwedbankAccountEntity signAndConfirmNewRecipient(CreateRecipientResponse createRecipientResponse,
            Function<BaseInfoPaymentResponse,Optional<SwedbankAccountEntity>> findNewRecipientFunction) throws Exception {

        return signTransfer(createRecipientResponse.getLinks().getSign())
                .flatMap(this::getConfirmResponse)
                .flatMap(findNewRecipientFunction)
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED).setEndUserMessage(
                        context.getCatalog().getString(TransferExecutionException.EndUserMessage.NEW_RECIPIENT_FAILED))
                        .build());
    }

    private Optional<BaseInfoPaymentResponse> getConfirmResponse(LinkEntity url) {
        try {
            return Optional.of(createTransferRequest(RequestMethod.PUT,
                    BASE_URL + url.getUri(),
                    BaseInfoPaymentResponse.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getDestinationName(final Transfer transfer) {

        return transfer.getDestination().getName()
                .orElseGet(() -> requestRecipientNameSupplemental().orElseThrow(() -> TransferExecutionException
                        .builder(SignableOperationStatuses.CANCELLED)
                        .setMessage("Could not get recipient name from user")
                        .setEndUserMessage(context.getCatalog()
                                .getString(TransferExecutionException.EndUserMessage.NEW_RECIPIENT_NAME_ABSENT))
                        .build()));
    }

    private Optional<String> requestRecipientNameSupplemental() {
        // If we're adding the recipient, we need to ask the user to name it.

        Field nameField = getNameField();
        List<Field> fields = Lists.newArrayList(nameField);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = context.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return Optional.empty();
        }

        Map<String, String> answers = SerializationUtils.deserializeFromString(supplementalInformation,
                FIELDS_TYPE_REFERENCE);

        return Optional.ofNullable(answers.get("name"));
    }

    private Field getNameField() {
        Field nameField = new Field();
        nameField.setDescription(context.getCatalog().getString("Recipient name"));
        nameField.setName("name");
        nameField.setPattern(".+");
        nameField.setHelpText(context.getCatalog().getString("Because this is the first time you transfer money to this"
                + " account, you'll need to register a name for it."));
        return nameField;
    }

    /**
     * Delete a set of transfer groups (used when cancelling injected transfers).
     */
    private void deleteTransfers(List<TransferTransactionGroupEntity> transactionsGroups) {
        try {
            for (TransferTransactionGroupEntity transactionsGroup : transactionsGroups) {
                for (TransferTransactionEntity transaction : transactionsGroup.getTransactions()) {
                    deleteTransfer(transaction);
                }
            }
        } catch (Exception deleteException) {
            log.warn("Could not delete transfers in outbox. "
                    + "If unsigned transfers are left here, user could end up in a deadlock.", deleteException);
        }
    }

    private void deleteTransfer(TransferTransactionEntity transaction) {
        ClientResponse response = createClientRequest(BASE_URL + transaction.getLinks().getSelf().getUri(),
                false)
                .delete(ClientResponse.class);

        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);
            String errorMessages = Joiner.on(", ").join(errorResponse.getErrorMessages().getAll());
            log.warn(String.format("#Swedbank-v5 - Delete transfer - Error messages: %s", errorMessages));
        }
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void update(Transfer transfer) throws Exception {
        ProfilesHandler.BankProfile bankProfile = getAndActivateTransferProfile();
        ProfileMenu profileMenu = bankProfile.getProfileMenu();

        switch (transfer.getType()) {
        case EINVOICE:
            approveEInvoice(profileMenu, transfer);
            break;
        case PAYMENT:
            updatePayment(profileMenu, transfer);
            break;
        default:
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }
    }

    private void approveEInvoice(ProfileMenu profileMenu, Transfer transfer) throws Exception {
        final Transfer originalTransfer = SerializationUtils.deserializeFromString(
                transfer.getPayload().get(TransferPayloadType.ORIGINAL_TRANSFER), Transfer.class);

        if (originalTransfer == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No original transfer on payload to compare with.").build();
        }

        Optional<InvoiceDetails> eInvoiceDetails = findMatchingInvoiceDetails(profileMenu, originalTransfer);

        if (!eInvoiceDetails.isPresent()) {
            throw TransferExecutionException
                    .builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            context.getCatalog().getString(
                                    TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        // Validate source and execute the update
        TransactionAccountEntity sourceAccount = validateSourceAccountFromEInvoice(transfer.getSource(),
                eInvoiceDetails.get().getFromAccountGroups());
        executeUpdate(profileMenu, transfer, sourceAccount, eInvoiceDetails.get());
    }

    private void updatePayment(ProfileMenu profileMenu, Transfer transfer) throws Exception {

        // Get confirmed payments
        ClientResponse paymentClientResponse = createClientRequest(
                BASE_URL + profileMenu.getMenuItem("PaymentConfirmed").getUri(), false)
                .get(ClientResponse.class);

        if (paymentClientResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            throw TransferExecutionException
                    .builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not fetch upcoming payments, status:" + paymentClientResponse.getStatus())
                    .setEndUserMessage(context.getCatalog()
                            .getString(TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED))
                    .build();
        }

        ConfirmedPaymentResponse paymentResponse = paymentClientResponse.getEntity(ConfirmedPaymentResponse.class);

        final Transfer originalTransfer = SerializationUtils.deserializeFromString(
                transfer.getPayload().get(TransferPayloadType.ORIGINAL_TRANSFER), Transfer.class);

        // Get the payment to update in the list of confirmed payments
        InvoiceDetails paymentToUpdate = getPaymentToUpdate(paymentResponse, originalTransfer);

        // Validate source account and then execute the update
        validateSourceAccountFromInvoice(transfer.getSource(), paymentToUpdate.getFromAccount());
        executeUpdate(profileMenu, transfer, paymentToUpdate.getFromAccount(), paymentToUpdate);
    }

    private InvoiceDetails getPaymentToUpdate(ConfirmedPaymentResponse paymentResponse, Transfer originalTransfer) {

        Optional<InvoiceDetails> payment = Optional.empty();

        groupLoop:
        for (TransferTransactionGroupEntity transferGroup : paymentResponse.getConfirmedTransactions()) {
            for (TransferTransactionEntity transaction : transferGroup.getTransactions()) {

                // The list of confirmed transactions also contain transfers, so if type isn't PAYMENT we want to
                // move on to next transaction.
                if (transaction == null || transaction.getLinks().getSelf() == null ||
                        !Objects.equal(transaction.getType(), "PAYMENT")) {
                    continue;
                }

                ConfirmedInvoice confirmedInvoice = createClientRequest(
                        BASE_URL + transaction.getLinks().getSelf().getUri(), false).get(ConfirmedInvoice.class);

                InvoiceDetails invoiceDetails = confirmedInvoice.getTransaction();
                invoiceDetails.getPayment().setAmount(invoiceDetails.getAmount());
                Transfer transferAtBank = invoiceDetails.toTransfer(true);
                transferAtBank.setType(TransferType.PAYMENT); // Need to set to find match, always PAYMENT BE.

                if (Objects.equal(originalTransfer.getHash(), transferAtBank.getHash())) {
                    payment = Optional.of(invoiceDetails);
                    break groupLoop;
                }
            }
        }

        if (!payment.isPresent()) {
            throw TransferExecutionException
                    .builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(context.getCatalog().getString(
                            TransferExecutionException.EndUserMessage.PAYMENT_NO_MATCHES))
                    .build();
        }

        return payment.get();
    }

    private void executeUpdate(ProfileMenu profileMenu, Transfer transfer, TransactionAccountEntity sourceAccount,
            InvoiceDetails invoiceDetails) throws Exception {

        PaymentRequest paymentRequest = createUpdateRequest(transfer, sourceAccount,
                invoiceDetails.getPayment());

        RegisteredPaymentResponse transferResponse = createPayment(profileMenu, invoiceDetails, paymentRequest);

        if (transferResponse.getRegisteredTransactions() != null) {
            signAndConfirmTransfer(profileMenu, transferResponse);
        }
    }

    private PaymentRequest createUpdateRequest(Transfer transfer, TransactionAccountEntity sourceAccount,
            TransferTransactionDetails payment) {

        PaymentReference reference = new PaymentReference();
        reference.setType(SwedbankAgentUtils.getReferenceTypeFor(transfer));
        reference.setValue(transfer.getDestinationMessage());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setReference(reference);
        paymentRequest.setNoteToSender(transfer.getSourceMessage());
        paymentRequest.setFormattedAmount(transfer.getAmount().getValue());
        paymentRequest.setFromAccountId(sourceAccount.getId());
        paymentRequest.setType(payment.getType());
        paymentRequest.setRecipientId(payment.getPayee().getId());
        paymentRequest.setEinvoiceReference(payment.getEinvoiceReference());
        paymentRequest.setFormattedDueDate(transfer.getDueDate(), context.getCatalog());

        return paymentRequest;
    }

    @Override
    public void execute(final Transfer transfer) throws Exception {
        ProfilesHandler.BankProfile bankProfile = getAndActivateTransferProfile();
        ProfileMenu profileMenu = bankProfile.getProfileMenu();

        SwedbankTransferResponseFilter swedbankTransferResponseFilter = new SwedbankTransferResponseFilter(context);
        client.addFilter(swedbankTransferResponseFilter);

        if (transfer.getType() == TransferType.BANK_TRANSFER) {
            executeBankTransfer(profileMenu, transfer);
        } else if (transfer.getType() == TransferType.PAYMENT) {
            executePayment(profileMenu, transfer);
        } else {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Not implemented.")
                    .setEndUserMessage("Not implemented.").build();
        }

        client.removeFilter(swedbankTransferResponseFilter);
    }

    private void executePayment(ProfileMenu profileMenu, Transfer transfer) throws Exception {

        validateNoUnsignedTransfers(profileMenu);

        // Validate transfer source information.
        final AccountIdentifier source = transfer.getSource();

        BaseInfoPaymentResponse baseInfoPaymentResponse =
                createClientRequest(BASE_URL + profileMenu.getMenuItem("PaymentBaseinfo").getUri(), false)
                .get(BaseInfoPaymentResponse.class);

        TransactionAccountEntity sourceAccount = validateSourceAccount(source,
                baseInfoPaymentResponse.getPaymentFromAccounts());

        // Validate transfer destination information and get id for destination account.
        String destinationAccountId = getDestinationAccountIdForPayment(profileMenu, transfer, baseInfoPaymentResponse);

        // Create the payment request.
        PaymentRequest paymentRequest = createPaymentRequest(transfer, sourceAccount, destinationAccountId);

        // Make the payment.
        RegisteredPaymentResponse paymentResponse = createPayment(profileMenu, null, paymentRequest);
        signAndConfirmTransfer(profileMenu, paymentResponse);
    }

    private String getDestinationAccountIdForPayment(ProfileMenu profileMenu, Transfer transfer, BaseInfoPaymentResponse baseInfoPaymentResponse)
        throws Exception {

        final AccountIdentifier destination = transfer.getDestination();
        Optional<PaymentAccountEntity> destinationAccount = GeneralUtils.find(destination,
                baseInfoPaymentResponse.getPayment().getPayees());

        if (!destinationAccount.isPresent()) {
            SwedbankAccountEntity newDestinationAccount = createSignedPayee(profileMenu, transfer);
            return newDestinationAccount.getId();
        }

        return destinationAccount.get().getId();
    }

    private PaymentRequest createPaymentRequest(Transfer transfer, TransactionAccountEntity sourceAccount,
            String destinationAccountId) {

        PaymentReference reference = new PaymentReference();
        reference.setType(SwedbankAgentUtils.getReferenceTypeFor(transfer));
        reference.setValue(transfer.getDestinationMessage());

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setReference(reference);
        paymentRequest.setNoteToSender(transfer.getSourceMessage());
        paymentRequest.setFormattedAmount(transfer.getAmount().getValue());
        paymentRequest.setFromAccountId(sourceAccount.getId());
        paymentRequest.setType("DOMESTIC");
        paymentRequest.setRecipientId(destinationAccountId);
        paymentRequest.setFormattedDueDate(transfer.getDueDate(), context.getCatalog());

        return paymentRequest;
    }

    private void executeBankTransfer(ProfileMenu profileMenu, final Transfer transfer) throws Exception {

        validateNoUnsignedTransfers(profileMenu);

        // Get transfer account information.
        BaseInfoPaymentResponse baseInfoPaymentResponse =
                createClientRequest(BASE_URL + profileMenu.getMenuItem("PaymentBaseinfo").getUri(), false)
                .get(BaseInfoPaymentResponse.class);

        // Create the transfer request.
        TransferRequest transferRequest = createTransferRequestForBankTransfer(profileMenu, transfer, baseInfoPaymentResponse);

        // Execute the transfer.
        ClientResponse transferClientResponse =
                createClientRequest(BASE_URL + profileMenu.getMenuItem("PaymentRegisterTransfer").getUri(), true)
                .post(ClientResponse.class, transferRequest);

        int status = transferClientResponse.getStatus();

        if (status != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = transferClientResponse.getEntity(ErrorResponse.class);
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED).setEndUserMessage(
                    getEndUserMessage(errorResponse, TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED))
                    .build();
        }

        RegisteredPaymentResponse transferResponse = createClientRequest(
                BASE_URL
                        + transferClientResponse.getEntity(RegisteredPaymentResponse.class).getLinks().getNext()
                        .getUri(), false).get(RegisteredPaymentResponse.class);

        signAndConfirmTransfer(profileMenu, transferResponse);
    }

    private TransferRequest createTransferRequestForBankTransfer(ProfileMenu profileMenu, Transfer transfer,
            BaseInfoPaymentResponse baseInfoPaymentResponse) throws Exception {

        // Ensure correct formatting of transfer messages.
        boolean isTransferBetweenSameUserAccounts = GeneralUtils.find(transfer.getDestination(),
                baseInfoPaymentResponse.getTransferToAccounts()).isPresent();
        TransferMessageFormatter.Messages formattedMessages = transferMessageFormatter
                .getMessages(transfer, isTransferBetweenSameUserAccounts);

        TransactionAccountEntity sourceAccount = validateSourceAccount(transfer.getSource(),
                baseInfoPaymentResponse.getTransferFromAccounts());

        Optional<TransactionAccountEntity> destinationAccount = GeneralUtils.find(transfer.getDestination(),
                baseInfoPaymentResponse.getAllRecipientAccounts());

        // If a registered recipient wasn't found for the destination account, try to register it.
        String recipientAccountId;

        if (destinationAccount.isPresent()) {
            recipientAccountId = destinationAccount.get().getId();
        } else {
            SwedbankAccountEntity newDestinationAccount = createSignedRecipient(profileMenu, transfer);
            recipientAccountId = newDestinationAccount.getId();
        }

        // Create the request.
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setNoteToSender(formattedMessages.getSourceMessage());
        transferRequest.setNoteToRecipient(formattedMessages.getDestinationMessage());
        transferRequest.setAmount(Integer.toString(transfer.getAmount().getValue().intValue()));
        transferRequest.setFromAccountId(sourceAccount.getId());
        transferRequest.setRecipientId(recipientAccountId);

        return transferRequest;
    }

    private void signAndConfirmTransfer(ProfileMenu profileMenu, RegisteredPaymentResponse transferResponse) throws Exception {
        ConfirmedPaymentResponse confirmedPaymentResponse = null;

        try {

            Optional<LinkEntity> confirmTransferLink = Optional.ofNullable(transferResponse.getLinks().getNext());

            // Sign the transfer if needed.

            if (!confirmTransferLink.isPresent()) {
                confirmTransferLink = signTransfer(transferResponse.getLinks().getSign());

                // Remove the transfers if the signing failed.

                if (!confirmTransferLink.isPresent()) {
                    transferResponse = createClientRequest(
                            BASE_URL + profileMenu.getMenuItem("PaymentRegistered").getUri(), false)
                            .get(RegisteredPaymentResponse.class);

                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setMessage("No confirm transfer link found. Transfer failed.")
                            .setEndUserMessage(context.getCatalog()
                                    .getString(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED))
                            .build();
                }
            }

            // Confirm the transfer.

            confirmedPaymentResponse = createTransferRequest(
                    RequestMethod.PUT,
                    BASE_URL + confirmTransferLink.get().getUri(),
                    ConfirmedPaymentResponse.class);

            if (!confirmedPaymentResponse.getRejectedTransactions().isEmpty()) {

                // TODO: This might need to be more dynamic.
                ErrorMessage rejectionCause = confirmedPaymentResponse.getRejectedTransactions().get(0)
                        .getTransactions().get(0).getRejectionCauses().get(0);
                String errorMessage = context.getCatalog().getString(rejectionCause.getMessage());
                if (Strings.isNullOrEmpty(errorMessage)) {
                    errorMessage = context.getCatalog()
                            .getString(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED);
                }
                throw TransferExecutionException.builder(rejectionCause.getSignableOperationStatus())
                        .setEndUserMessage(errorMessage).build();
            }

        } catch (Exception e) {
            if (confirmedPaymentResponse != null && !confirmedPaymentResponse.getRejectedTransactions().isEmpty()) {
                deleteTransfers(confirmedPaymentResponse.getRejectedTransactions());
            } else {
                deleteTransfers(transferResponse.getRegisteredTransactions());
            }
            throw e;
        }
    }

    /**
     * This method is used in all cases a payment is made, that is approve e-invoice, update payment
     * and execute payment. However, the different cases have different ways of doing the payment.
     */
    private RegisteredPaymentResponse createPayment(ProfileMenu profileMenu, InvoiceDetails invoiceDetails, PaymentRequest paymentRequest)
            throws Exception {
        ClientResponse transferClientResponse;
        boolean isUpdate = false;

        if (invoiceDetails != null && invoiceDetails.getLinks() != null &&
                invoiceDetails.getLinks().getEdit() != null) {

            // This is an update of an existing payment.

            isUpdate = true;
            transferClientResponse = createClientRequest(
                    BASE_URL + invoiceDetails.getLinks().getEdit().getUri(), true).put(
                    ClientResponse.class, paymentRequest);
        } else {

            // This a post of an e-invoice or a payment.

            transferClientResponse = createClientRequest(
                    BASE_URL + profileMenu
                            .getMenuItem("PaymentRegisterPayment").getUri(), true)
                    .post(ClientResponse.class, paymentRequest);
        }

        checkPaymentPostResponseStatus(transferClientResponse, isUpdate, paymentRequest);

        // Get registered payment.
        return createClientRequest(
                BASE_URL + transferClientResponse.getEntity(RegisteredPaymentResponse.class).getLinks().getNext()
                        .getUri(), false).get(RegisteredPaymentResponse.class);
    }

    /**
     * Checks status of the response when posting or updating a payment, throws a TransferExecutionException with an
     * appropriate error message if status isn't 200.
     */
    private void checkPaymentPostResponseStatus(ClientResponse transferClientResponse, boolean isPaymentUpdate,
            PaymentRequest paymentRequest) {

        int status = transferClientResponse.getStatus();

        if (status != HttpStatusCodes.STATUS_CODE_OK) {
            ErrorResponse errorResponse = transferClientResponse.getEntity(ErrorResponse.class);
            String errorMessage = isPaymentUpdate ?
                    getEndUserMessage(errorResponse, TransferExecutionException.EndUserMessage.PAYMENT_UPDATE_FAILED) :
                    getEndUserMessage(errorResponse, TransferExecutionException.EndUserMessage.PAYMENT_CREATE_FAILED);

            // This is a temporary error check to investigate if Swedbank accepts (valid) OCR strings with spaces. If
            // they do not, we might have to remove all spaces in the OCR strings we send.
            if (Objects.equal(paymentRequest.getReference().getType(), "OCR")) {
                log.error(errorMessage + " - Received http status code " + status
                        + " during payment with OCR. Check if OCR has spaces.");
            }

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(errorMessage).build();
        }
    }

    private List<InvoiceDetails> getInvoices(ProfileMenu profileMenu) throws Exception {
        List<InvoiceDetails> invoices = Lists.newArrayList();

        for (Invoice invoice : getInvoiceList(profileMenu)) {
            invoices.add(getDetailsFor(invoice));
        }

        return invoices;
    }

    private List<Invoice> getInvoiceList(ProfileMenu profileMenu) throws Exception {
        if (!profileMenu.hasMenuItem("MessageReminders")) {
            // "Youth" users do not have this key. Working theory is that they are not allowed
            // to have e-invoices.
            return Lists.newArrayList();
        }

        RemindersResponse reminderResponse =
                createClientRequest(BASE_URL + profileMenu.getMenuItem("MessageReminders").getUri(), false)
                        .get(RemindersResponse.class);

        if (reminderResponse == null || reminderResponse.getIncomingEinvoices().getCount() == 0) {
            return Lists.newArrayList();
        }

        InvoicesResponse invoicesResponse =
                createClientRequest(
                        BASE_URL + reminderResponse.getIncomingEinvoices().getLinks().getNext().getUri(), false)
                        .get(InvoicesResponse.class);

        return invoicesResponse.getEinvoices();
    }

    private Optional<InvoiceDetails> findMatchingInvoiceDetails(ProfileMenu profileMenu, final Transfer originalTransfer) throws Exception {

        for (Invoice invoice : getInvoiceList(profileMenu)) {
            InvoiceDetails invoiceDetails = getDetailsFor(invoice);

            if (invoiceDetails == null) {
                continue;
            }

            invoiceDetails.getPayment().setType("EINVOICE");
            if (Objects.equal(originalTransfer.getHashIgnoreSource(),
                    invoiceDetails.toTransfer(false).getHashIgnoreSource())) {
                return Optional.of(invoiceDetails);
            }
        }

        return Optional.empty();
    }

    private InvoiceDetails getDetailsFor(Invoice invoice) throws Exception {
        return createClientRequest(
                BASE_URL + invoice.getLinks().getNext().getUri(), false).get(InvoiceDetails.class);
    }

    private void validateNoUnsignedTransfers(ProfileMenu profileMenu) throws Exception {
        RegisteredPaymentResponse registeredTransferResponse = createClientRequest(
                BASE_URL + profileMenu.getMenuItem("PaymentRegistered").getUri(), false)
                .get(RegisteredPaymentResponse.class);

        if (!registeredTransferResponse.getRegisteredTransactions().isEmpty()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(context.getCatalog()
                            .getString(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                    .build();
        }
    }

    private TransactionAccountEntity validateSourceAccount(final AccountIdentifier source,
            List<TransactionAccountEntity> fromAccounts) {
        Optional<TransactionAccountEntity> sourceAccount = GeneralUtils.find(source, fromAccounts);

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            context.getCatalog().getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        return sourceAccount.get();
    }

    private void validateSourceAccountFromInvoice(AccountIdentifier source, TransactionAccountEntity fromAccount) {

        if (!Objects.equal(source.getIdentifier(DEFAULT_FORMATTER),
                fromAccount.generalGetAccountIdentifier().getIdentifier(DEFAULT_FORMATTER))) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            context.getCatalog().getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }
    }

    private TransactionAccountEntity validateSourceAccountFromEInvoice(AccountIdentifier source,
            List<TransactionAccountGroupEntity> transactionAccountGroups) {
        Optional<TransactionAccountEntity> sourceAccount = Optional.empty();

        for (TransactionAccountGroupEntity accountGroup : transactionAccountGroups) {
            sourceAccount = GeneralUtils.find(source, accountGroup.getAccounts());
            if (sourceAccount.isPresent()) {
                break;
            }
        }

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            context.getCatalog().getString(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        return sourceAccount.get();
    }

    private String generateAuthorization() {
        // Create a fake hash (UUID-like) to use as a persistent device ID

        String deviceId = StringUtils.hashAsUUID("TINK-" + profileParameters.getName() + "-"
                + credentials.getField(Field.Key.USERNAME));
        String apiKey = profileParameters.getApiKey();

        Base64 base64 = new Base64(100, null, true);

        return new String(base64.encode((apiKey + ":" + deviceId).getBytes(Charsets.US_ASCII)));
    }

    private Loan getLoanDetails(Account account, AccountEntity enrichedAccountEntity) {
        if (enrichedAccountEntity.getLinks() == null || enrichedAccountEntity.getLinks().getNext() == null) {
            return null;
        }

        LinkEntity entity = enrichedAccountEntity.getLinks().getNext();

        try {
            // Getting a String response here just in order to put it on the LoanData object

            // TODO: Use LoanResponse instead of String here when we remove the serialized response from LoanData

            String loanResponseString = createClientRequest(BASE_URL + entity.getUri(), false)
                    .get(String.class);
            LoanResponse loanResponse = MAPPER.readValue(loanResponseString, LoanResponse.class);
            return loanResponse != null ? loanResponse.toLoan(account, loanResponseString) : null;

        } catch (Exception e) {
            log.warn(String.format("Couldn't fetch loan data! %s", enrichedAccountEntity), e);
        }

        return null;
    }

    private boolean hasValidProfile(ProfileResponse profileResponse) {
        return !profileParameters.isSavingsBank() ?
                profileResponse.isHasSwedbankProfile() : profileResponse.isHasSavingbankProfile();
    }

    public static boolean isValidCreditCardAccountNumber(String accountNumber) {
        return ALLOWED_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN.matcher(accountNumber).matches();
    }

    private boolean isValidGenericSwedbankAccountNumber(String accountNumber) {
        return ALL_ALLOWED_ACCOUNT_BANKIDS.matcher(Preconditions.checkNotNull(accountNumber)).matches();
    }

    public static boolean isValidNonCreditCardAccountNumber(String accountNumber) {
        return ALLOWED_NON_CREDIT_CARD_ACCOUNT_BANKIDS_PATTERN.matcher(accountNumber).matches();
    }

    private ProfileResponse getProfileResponse() {
        ClientResponse profileClientResponse = createClientRequest(BASE_URL_V5 + "/profile/", false).get(
                ClientResponse.class);

        return profileClientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED ?
                null : profileClientResponse.getEntity(ProfileResponse.class);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        if (!authenticateWithMobiltBankId()) {
            return false;
        }

        ProfileResponse profileResponse = getProfileResponse();
        Preconditions.checkNotNull(profileResponse);
        checkProfileResponseErrorMessages(profileResponse);

        // Mismatch! User logs in through Swedbank provider but has only a Savingsbank profile, or vice versa.
        if (credentials.getType() == CredentialsTypes.MOBILE_BANKID && modeAndProfileMismatch(profileResponse)) {
            if (!profileParameters.isSavingsBank()) {
                throw LoginError.NOT_CUSTOMER.exception(UserMessage.WRONG_BANK_SWEDBANK.getKey());
            } else {
                throw LoginError.NOT_CUSTOMER.exception(UserMessage.WRONG_BANK_SAVINGSBANK.getKey());
            }
        }

        if (!hasValidProfile(profileResponse)) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        // We're in the correct profile and we have data, select the profile
        // and go fetch all of our account information.
        // Cache the details response for later checking of authorized URI's
        List<BankEntity> bankEntityList = profileResponse.getBanks();
        if (bankEntityList.isEmpty()) {
            // This check protects against errors if the user has engagements in i.e. swedbank but the user has
            // logged in to swedbank-youth.
            return handleLoginToIncorrectProvider();
        } else if (bankEntityList.size() == 1) {
            // TODO: We might need to support multiple banks here, but it complicates things with transfers. Current
            // thesis is that this is a legacy thing.

            // Activate a profile.
            BankEntity bankEntity = bankEntityList.get(0);
            ProfileMenu profileMenu = activateProfile(bankEntity);
            EngagementOverviewResponse engagementOverView = fetchEngagementOverviewNoActivation(profileMenu);
            profilesHandler.addBankProfile(bankEntity, profileMenu, engagementOverView, true, true);
        } else {
            try {
                log.info("Multiple banks detected: " + MAPPER.writeValueAsString(bankEntityList));
            } catch (JsonProcessingException e) {
                // Just log an swallow this - No reason to not continue login
                log.warn(e.getMessage(), e);
            }

            for (BankEntity bank : bankEntityList) {
                try {
                    log.info("Multiple banks - activating profile " + bank.getName());
                    ProfileMenu profileMenu = activateProfile(bank);
                    EngagementOverviewResponse engagementOverView = fetchEngagementOverviewNoActivation(profileMenu);
                    profilesHandler.addBankProfile(bank, profileMenu, engagementOverView, true, true);
                } catch (Exception e) {
                    log.error("Multiple banks - could not activate profile " + bank.getName(), e);
                }
            }
        }

        return true;
    }

    private void checkProfileResponseErrorMessages(ProfileResponse profileResponse) throws  AuthorizationException {

        if (profileResponse.getErrorMessages() == null) {
            return;
        }

        List<ErrorMessage> errorMessages = profileResponse.getErrorMessages().getAll();

        if (errorMessages.isEmpty()) {
            return;
        }

        ErrorMessage errorMessage = errorMessages.get(0);
        switch (errorMessage.getCode().toLowerCase()) {
        case "authorization_failed":
        case "not_found":
            log.info(String.format("No %s profile for user.", profileParameters.name));
            throw AuthorizationError.UNAUTHORIZED.exception();
        default:
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Login Failed with code: %s, message: %s",
                            errorMessage.getCode(),
                            errorMessage.getMessage()
                    )
            );
        }
    }

    private boolean handleLoginToIncorrectProvider() throws LoginException {
        switch (credentials.getProviderName()) {
        case "swedbank-bankid":
            throw LoginError.NOT_SUPPORTED.exception(UserMessage.SWEDBANK_YOUTH.getKey());
        case "savingsbank-bankid":
            throw LoginError.NOT_SUPPORTED.exception(UserMessage.SAVINGSBANK_YOUTH.getKey());
        case "swedbank-bankid-youth":
            throw LoginError.NOT_CUSTOMER.exception(UserMessage.USE_SWEDBANK.getKey());
        case "savingsbank-bankid-youth":
            throw LoginError.NOT_CUSTOMER.exception(UserMessage.USE_SAVINGSBANK.getKey());
        default:
            throw new IllegalStateException(
                    String.format("#login-refactoring - [Login Failed got unknown providerName]: %s",
                            credentials.getProviderName()));
        }
    }

    @Override
    public void logout() throws Exception {
        createClientRequest(BASE_URL + profilesHandler.getActiveProfile().getProfileMenu()
                .getMenuItem("IdentificationLogout").getUri(), false).put();
    }

    private boolean modeAndProfileMismatch(ProfileResponse profileResponse) {
        if (!profileParameters.isSavingsBank()) {
            return !profileResponse.isHasSwedbankProfile() && profileResponse.isHasSavingbankProfile();
        } else {
            return !profileResponse.isHasSavingbankProfile() && profileResponse.isHasSwedbankProfile();
        }
    }

    private Optional<Account> constructTinkAccount(AccountEntity accountEntity, AccountTypes accountType) {
        if (accountType != AccountTypes.CREDIT_CARD &&
                (accountEntity.getBalance() == null || accountEntity.getBalance().replaceAll("[^0-9]", "").isEmpty())) {
            return Optional.empty();
        }

        if (accountType == AccountTypes.SAVINGS && "ISK".equalsIgnoreCase(accountEntity.getType())) {
            return Optional.empty();
        }

        if (Sets.newHashSet(AccountTypes.SAVINGS, AccountTypes.OTHER).contains(accountType)
                && StringUtils.trimToNull(accountEntity.getName()) != null) {
            final String lowerCaseName = accountEntity.getName().toLowerCase();

            // Trying to better classify the accounts using heuristics.
            if (lowerCaseName.contains("fond") || lowerCaseName.contains("investeringssparande")) {
                accountType = AccountTypes.INVESTMENT;
            } else if (lowerCaseName.contains("spar")) {
                accountType = AccountTypes.SAVINGS;
            }
        }

        Account account = new Account();
        account.setName(accountEntity.getName());
        account.setAccountNumber(accountEntity.getFullyFormattedNumber());
        account.setBalance(parseAmount(accountEntity.getBalance()));
        account.setBankId(accountEntity.getFullyFormattedNumber());
        account.setType(accountType);

        // Since loan accounts from Swedbank cannot be transferred to, don't give them a valid identifier in Tink.

        if (accountType != AccountTypes.LOAN && accountType != AccountTypes.CREDIT_CARD) {
            account.putIdentifier(new SwedishIdentifier(accountEntity.getFullyFormattedNumber()));
        }

        return Optional.of(account);
    }

    private void refreshInvestmentAccounts() {
        refreshHoldings();
    }

    private Optional<HoldingsResponse> fetchHoldings() {
        // Swedbank haven't added investment data to their new API yet.
        // Fetch the profile response for the v4 API.
        ProfileResponse profileResponse = createClientRequest(BASE_URL + "/v4/profile/", false)
                .get(ProfileResponse.class);

        BankEntity bankEntity = profileResponse.getBanks().stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        LinkEntity profileLink = bankEntity.getPrivateProfile().getLinks().getNext();

        if (profileLink == null) {
            log.warn("could not get investment data - profile link was null");
            return Optional.empty();
        }

        ProfileMenu profileMenu = createClientRequest(BASE_URL + profileLink.getUri(), false)
                .post(ProfileMenu.class);

        MenuItem portfolioHoldings = profileMenu.getMenuItem("PortfolioHoldings");

        if (portfolioHoldings == null) {
            log.warn("could not get investment data - portfolio holdings relative link was not available");
            return Optional.empty();
        }

        // Some users are not authorized to fetch portfolio holdings.
        if (!portfolioHoldings.isAuthorizedURI(MenuItem.Method.GET)) {
            return Optional.empty();
        }

        return Optional.of(createClientRequest(BASE_URL + portfolioHoldings.getUri(), false)
                .get(HoldingsResponse.class));
    }

    private void refreshHoldings() {
        Optional<HoldingsResponse> holdingsResponse = fetchHoldings();

        if (!holdingsResponse.isPresent()) {
            return;
        }

        HoldingsResponse response = holdingsResponse.get();

        refreshEndowmentInsurances(response);
        refreshEquityTraders(response);
        refreshFundAccounts(response);
        refreshInvestmentSavings(response);
    }

    private void refreshEndowmentInsurances(HoldingsResponse holdingsResponse) {
        if (holdingsResponse.getEndowmentInsurances() == null) {
            log.warn("endowment insurances was null");
            return;
        }

        if (holdingsResponse.getEndowmentInsurances().isEmpty()) {
            return;
        }

        log.info("#Swedbank-Endowment Insurance " + SerializationUtils.serializeToString(holdingsResponse));

        holdingsResponse.getEndowmentInsurances().forEach(endowmentInsuranceEntity -> {
            // Only seen holdings of type FUND but we there are probably more types
            logResponseForUnknownTypes(endowmentInsuranceEntity);

            Optional<Account> account = endowmentInsuranceEntity.toAccount();
            if (!account.isPresent()) {
                return;
            }

            Portfolio portfolio = endowmentInsuranceEntity.toPortfolio();

            Map<String, EndowmentInsuranceHoldingEntity> holdingsByFuncCode = endowmentInsuranceEntity.getPlacements()
                    .stream()
                    .map(EndowmentInsurancePlacementEntity::getSubPlacements)
                    .flatMap(List::stream)
                    .map(EndowmentInsuranceSubPlacementEntity::getHoldings)
                    .flatMap(List::stream)
                    .filter(e -> e.getFundCode() != null)
                    .collect(Collectors.toMap(EndowmentInsuranceHoldingEntity::getFundCode, Function.identity()));

            List<Instrument> instruments = Lists.newArrayList();
            holdingsByFuncCode.forEach((fundCode , holding) -> {
                Optional<Instrument> optionalInstrument = holding.toInstrument();
                if (!optionalInstrument.isPresent()) {
                    return;
                }
                Instrument instrument = optionalInstrument.get();

                MarketInfoEntity marketInfo = getFundMarketInfo(fundCode);
                if (marketInfo.getIsincode() == null) {
                    return;
                }
                instrument.setIsin(marketInfo.getIsincode());
                instrument.setUniqueIdentifier(marketInfo.getIsincode() + holding.getFundCode());

                instruments.add(instrument);
            });
            portfolio.setInstruments(instruments);

            context.cacheAccount(account.get(), AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private void logResponseForUnknownTypes(EndowmentInsuranceEntity endowmentInsuranceEntity) {
        Optional<EndowmentInsuranceHoldingEntity> unknownInstrumentType = endowmentInsuranceEntity.getPlacements()
                .stream()
                .map(EndowmentInsurancePlacementEntity::getSubPlacements)
                .flatMap(List::stream)
                .map(EndowmentInsuranceSubPlacementEntity::getHoldings)
                .flatMap(List::stream)
                .filter(e -> e.getHoldingType() != null && (!Objects.equal("fund", e.getHoldingType().toLowerCase())
                        || !Objects.equal("cash", e.getHoldingType().toLowerCase())))
                .findFirst();

        if (!unknownInstrumentType.isPresent()) {
            return;
        }

        try {
            String logUnknownEntity = createClientRequest(
                    BASE_URL + endowmentInsuranceEntity.getLinks().getSelf().getUri(), false)
                    .get(String.class);
            log.info("#unkown-entity-logging: " + logUnknownEntity);
        } catch (Exception e) {
            log.info("#unkown-entity-logging failed");
        }
    }

    private void refreshEquityTraders(HoldingsResponse holdingsResponse) {
        if (holdingsResponse.getEquityTraders() == null) {
            log.warn("equity traders was null");
            return;
        }

        if (holdingsResponse.getEquityTraders().isEmpty()) {
            return;
        }

        log.info("#Swedbank-Equity Trader " + SerializationUtils.serializeToString(holdingsResponse));

        holdingsResponse.getEquityTraders().forEach(equityTradersEntity -> {
            Optional<Account> account = equityTradersEntity.toAccount();

            if (!account.isPresent()) {
                return;
            }

            Portfolio portfolio = equityTradersEntity.toPortfolio();

            List<Instrument> instruments = Lists.newArrayList();
            equityTradersEntity.getHoldings().stream()
                    .map(HoldingsEntity::getInstruments)
                    .flatMap(List::stream)
                    .forEach(instrumentsEntity -> {
                        instrumentsEntity.toInstrument().ifPresent(instruments::add);
            });
            portfolio.setInstruments(instruments);

            context.cacheAccount(account.get(), AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private void refreshFundAccounts(HoldingsResponse holdingsResponse) {
        if (holdingsResponse.getFundAccounts() == null) {
            log.warn("fund accounts was null");
            return;
        }

        if (holdingsResponse.getFundAccounts().isEmpty()) {
            return;
        }

        log.info("#Swedbank-Fund Account " + SerializationUtils.serializeToString(holdingsResponse));

        holdingsResponse.getFundAccounts().forEach(fundAccountsEntity -> {
            Optional<Account> account = fundAccountsEntity.toAccount();

            if (!account.isPresent()) {
                return;
            }

            Portfolio portfolio = fundAccountsEntity.toPortfolio();

            // Make a map were the keys are placement type and the values are maps.
            // The value maps have fundcode as key and fund entity as value.
            // We have to do this since we want to set placement type on the instruments and we also
            // need the fund codes to enrich the instruments with isin.
            Map<String, Map<String, FundHoldingsEntity>> fundByFundCodeByPlacementType =
                    fundAccountsEntity.getPlacements().stream()
                            .collect(Collectors.toMap(
                                    p -> p.getPlacementType().toLowerCase(),
                                    p -> p.getFundHoldings().stream()
                                            .collect(Collectors.toMap(
                                                    FundHoldingsEntity::getFundCode,
                                                    Function.identity()))));

            List<Instrument> instruments = Lists.newArrayList();
            fundByFundCodeByPlacementType.forEach((placementType, fundByFundCode) -> {
                fundByFundCode.forEach((fundCode, fundEntity) -> {
                    MarketInfoEntity fundMarketInfo = getFundMarketInfo(fundCode);

                    if (fundMarketInfo.getIsincode() == null) {
                        log.warn("isin code was null in market info");
                        return;
                    }

                    Optional<Instrument> instrument = fundEntity.toInstrument(
                            placementType, fundMarketInfo.getIsincode());

                    if (!instrument.isPresent()) {
                        return;
                    }

                    instruments.add(instrument.get());
                });
            });
            portfolio.setInstruments(instruments);

            context.cacheAccount(account.get(), AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private void refreshInvestmentSavings(HoldingsResponse holdingsResponse) {
        if (holdingsResponse.getInvestmentSavings() == null) {
            log.warn("investment savings accounts was null");
            return;
        }

        if (!holdingsResponse.getInvestmentSavings().isEmpty()) {
            log.info("#Swedbank-ISK " + SerializationUtils.serializeToString(holdingsResponse));
        }

        holdingsResponse.getInvestmentSavings().forEach(accountEntity -> {
            Optional<Account> account = accountEntity.toAccount();
            if (!account.isPresent()) {
                return;
            }

            Optional<Double> cashValue = accountEntity.getPlacements().stream()
                    .filter(e -> "ränteplaceringar".equalsIgnoreCase(e.getName().trim()))
                    .map(IskPlacementEntity::getSubPlacements)
                    .flatMap(List::stream)
                    .map(IskSubPlacementEntity::getHoldings)
                    .flatMap(List::stream)
                    .filter(he -> "cash".equalsIgnoreCase(he.getHoldingType()))
                    .map(IskHoldingEntity::getSettlement)
                    .map(SettlementEntity::getBalance)
                    .map(AmountEntity::getAmount)
                    .map(StringUtils::parseAmount)
                    .findFirst();

            Portfolio portfolio = accountEntity.toPortfolio(cashValue.orElse(null));

            Map<String, IskHoldingEntity> holdingsByFundCode = accountEntity.getPlacements().stream()
                    .map(IskPlacementEntity::getSubPlacements)
                    .flatMap(List::stream)
                    .map(IskSubPlacementEntity::getHoldings)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(
                            IskHoldingEntity::getFundCode,
                            sp -> sp));

            List<Instrument> instruments = Lists.newArrayList();
            holdingsByFundCode.forEach((fundCode, holding) -> {
                if (fundCode == null) {
                    return;
                }

                MarketInfoEntity fundMarketInfo = getFundMarketInfo(fundCode);

                holding.toInstrument(fundMarketInfo.getIsincode()).ifPresent(instruments::add);
            });

            portfolio.setInstruments(instruments);

            context.cacheAccount(account.get(), AccountFeatures.createForPortfolios(portfolio));
        });
    }

    private MarketInfoEntity getFundMarketInfo(String fundCode) {
        return createClientRequest(String.format(
                "https://auth.api.swedbank.se/TDE_DAP_Portal_REST_WEB/api/v4/fund/marketinfo/%s", fundCode), false)
                .get(MarketInfoEntity.class);
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case EINVOICES:
            try {
                updateEInvoices();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case TRANSFER_DESTINATIONS:
            try {
                updateTransferDestinations();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;

        case CHECKING_ACCOUNTS:
            updateCheckingAccounts();
            break;

        case CHECKING_TRANSACTIONS:
            updateCheckingTransactions();
            break;

        case SAVING_ACCOUNTS:
            updateSavingAccounts();
            break;

        case SAVING_TRANSACTIONS:
            updateSavingTransactions();
            break;

        case CREDITCARD_ACCOUNTS:
            updateCreditCardAccounts();
            break;

        case CREDITCARD_TRANSACTIONS:
            updateCreditCardTransactions();
            break;

        case LOAN_ACCOUNTS:
            updateLoanAccounts();
            break;

        case INVESTMENT_ACCOUNTS:
            try {
                refreshInvestmentAccounts();
            } catch (Exception e) {
                // Just catch this and exit gently.
                log.warn("Caught exception while logging investment data", e);
            }
            break;
        }
    }

    private void updateLoanAccounts() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            updateLoanAccounts(overviewResponse.getLoanAccounts());
        });
    }

    private void updateCreditCardAccounts() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateCreditCardAccounts(overviewResponse.getCardAccounts());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateCreditCardTransactions() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateAccountsAndTransactions(overviewResponse.getCardAccounts(), AccountTypes.CREDIT_CARD);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateCheckingAccounts() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateAccounts(overviewResponse.getTransactionAccounts(), AccountTypes.CHECKING);
                updateAccounts(overviewResponse.getTransactionDisposalAccounts(), AccountTypes.OTHER);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateCheckingTransactions() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateAccountsAndTransactions(overviewResponse.getTransactionAccounts(), AccountTypes.CHECKING);
                updateAccountsAndTransactions(overviewResponse.getTransactionDisposalAccounts(), AccountTypes.OTHER);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateSavingAccounts() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateAccounts(overviewResponse.getSavingAccounts(), AccountTypes.SAVINGS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void updateSavingTransactions() {
        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            EngagementOverviewResponse overviewResponse = bankProfile.getEngagementOverview();
            try {
                updateAccountsAndTransactions(overviewResponse.getSavingAccounts(), AccountTypes.SAVINGS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public void updateTransferDestinations() throws Exception {
        TransferDestinationsResponse response = new TransferDestinationsResponse();

        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            ProfileMenu profileMenu = bankProfile.getProfileMenu();

            try {
                TransferDestinationsResponse profileDestinationResponse = getActivatedProfileTransferDestinations(
                        profileMenu, context.getUpdatedAccounts());
                response.addDestinations(profileDestinationResponse.getDestinations());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        context.updateTransferDestinationPatterns(response.getDestinations());
    }

    public void updateEInvoices() throws Exception {
        List<Transfer> eInvoices = Lists.newArrayList();

        profilesHandler.getProfiles().forEach((bankName, bankProfile) -> {
            if (!profilesHandler.isActiveProfile(bankProfile)) {
                activateProfile(bankProfile);
            }

            ProfileMenu profileMenu = bankProfile.getProfileMenu();

            try {
                eInvoices.addAll(getActivatedProfileEInvoices(profileMenu));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        context.updateEinvoices(eInvoices);
    }

    private void updateAccounts(List<AccountEntity> accountEntities, final AccountTypes accountType) throws Exception {
        Set<AccountTypes> handledAccountTypes = Sets.newHashSet(
                AccountTypes.CHECKING,
                AccountTypes.SAVINGS,
                AccountTypes.OTHER
        );

        for (AccountEntity accountEntity : accountEntities) {
            Optional<Account> optionalAccount = constructTinkAccount(accountEntity, accountType);
            if (!optionalAccount.isPresent()) {
                continue;
            }

            Account account = optionalAccount.get();

            if (handledAccountTypes.contains(account.getType())) {
                AccountFeatures assets = fetchAccountFeatures(account, accountEntity);

                // TODO - Remove call to updateTransactionalAccountDetails() if logging reveals there are nochange
                // between the updated account fields
                updateTransactionalAccountDetails(account, accountEntity);

                Preconditions.checkState(
                        isValidNonCreditCardAccountNumber(Preconditions.checkNotNull(account.getBankId())),
                        "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

                context.cacheAccount(account, assets);
            }
        }
    }

    /**
     * Remove method if logging reveals there are no change between the updated account fields
     */
    private void updateTransactionalAccountDetails(Account account, AccountEntity accountEntity) throws Exception {
        if (!Strings.isNullOrEmpty(accountEntity.getId())) {
            Optional<String> nextDetailsURI = accountEntity.getTransactionsURI();

            if (nextDetailsURI.isPresent()) {
                AccountEntity accountDetails = fetchTransactions(nextDetailsURI.get()).getAccount();

                account.setName(accountDetails.getName());
                account.setBankId(accountDetails.getFullyFormattedNumber());
                account.setAccountNumber(accountDetails.getFullyFormattedNumber());
            }
        }
    }

    private void updateCreditCardAccounts(List<AccountEntity> accountEntities) throws Exception {
        for (AccountEntity accountEntity : accountEntities) {
            Optional<String> nextDetailsURI = accountEntity.getTransactionsURI();

            if (nextDetailsURI.isPresent()) {
                Optional<Account> optionalAccount = constructTinkAccount(accountEntity, AccountTypes.CREDIT_CARD);
                if (!optionalAccount.isPresent()) {
                    continue;
                }

                Account account = optionalAccount.get();

                CardAccountEntity cardAccountEntity = fetchTransactions(nextDetailsURI.get()).getCardAccount();
                updateCreditCardAccountDetails(account, cardAccountEntity);

                Preconditions.checkState(
                        isValidCreditCardAccountNumber(Preconditions.checkNotNull(account.getBankId())),
                        "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

                context.cacheAccount(account);
            }
        }
    }

    private void updateCreditCardAccountDetails(Account account, CardAccountEntity cardAccountEntity) {
        account.setName(cardAccountEntity.getName());
        account.setBalance(calculateCardBalance(cardAccountEntity));
        account.setAvailableCredit(parseAmount(cardAccountEntity.getAvailableAmount()));
        account.setBankId(cardAccountEntity.getCardNumber());
        account.setAccountNumber(cardAccountEntity.getCardNumber());
    }

    private double calculateCardBalance(CardAccountEntity cardAccountEntity) {
        if (!Strings.isNullOrEmpty(cardAccountEntity.getReservedAmount())) {
            return parseAmount(cardAccountEntity.getCurrentBalance()) + parseAmount(
                    cardAccountEntity.getReservedAmount());
        }
        return parseAmount(cardAccountEntity.getCurrentBalance());
    }

    private void updateLoanAccounts(List<AccountEntity> accountEntities) {
        for (AccountEntity accountEntity : accountEntities) {
            refreshTransactionLessAccount(accountEntity, AccountTypes.LOAN);
        }
    }

    private TransferDestinationsResponse getActivatedProfileTransferDestinations(ProfileMenu profileMenu, List<Account> accounts)
            throws Exception {
        TransferDestinationsResponse response = new TransferDestinationsResponse();

        response.addDestinations(getAccountDestinations(profileMenu, accounts, DestinationType.TRANSFER));
        response.addDestinations(getAccountDestinations(profileMenu, accounts, DestinationType.PAYMENT));

        return response;
    }

    private ProfileMenu activateProfile(ProfilesHandler.BankProfile bankProfile) {
        ProfileMenu profileMenu = activateProfile(bankProfile.getBank());
        profilesHandler.setActiveProfile(bankProfile);

        return profileMenu;
    }

    private ProfileMenu activateProfile(BankEntity bank) {
        log.info("Activating profile " + bank.getName());

        ProfileMenu profileMenu = createClientRequest(
                BASE_URL + bank.getPrivateProfile().getLinks().getNext().getUri(),false)
                .post(ProfileMenu.class);

        return profileMenu;
    }

    private EngagementOverviewResponse fetchEngagementOverviewNoActivation(ProfileMenu profileMenu) {
        return createClientRequest(BASE_URL + profileMenu.getMenuItem("EngagementOverview").getUri(), false)
                .get(EngagementOverviewResponse.class);
    }

    private AccountFeatures fetchAccountFeatures(Account account, AccountEntity accountEntity) {
        if (account.getType().equals(AccountTypes.LOAN)) {
            return AccountFeatures.createForLoan(
                    getLoanDetails(account, accountEntity));
        }

        return AccountFeatures.createEmpty();
    }

    private AccountTransactionsResponse fetchTransactions(String URI) throws Exception {
        String URL = BASE_URL + URI;

        return sendRetryableGetRequest(URL, AccountTransactionsResponse.class);
    }

    private List<Transaction> getUpcomingTransactionsForBankId(String bankId) throws Exception {
        if (upcomingTransactionsByBankId == null) {
            fetchUpcomingTransactions();
        }

        if (Strings.isNullOrEmpty(bankId) || !upcomingTransactionsByBankId.containsKey(bankId)) {
            return Lists.newArrayList();
        }

        return upcomingTransactionsByBankId.get(bankId);
    }

    private void fetchUpcomingTransactions() throws Exception {
        upcomingTransactionsByBankId = Maps.newHashMap();

        ProfilesHandler.BankProfile bankProfile = profilesHandler.getActiveProfile();
        ProfileMenu profileMenu = bankProfile.getProfileMenu();

        ClientResponse transferClientResponse = createClientRequest(
                BASE_URL + profileMenu.getMenuItem("PaymentConfirmed").getUri(),
                false)
                .get(ClientResponse.class);

        if (transferClientResponse.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            return;
        }

        cacheUpcomingTransactions(transferClientResponse.getEntity(ConfirmedPaymentResponse.class));
    }

    private void cacheUpcomingTransactions(ConfirmedPaymentResponse confirmedPaymentResponse) throws Exception {

        for (TransferTransactionGroupEntity transferGroup : confirmedPaymentResponse.getConfirmedTransactions()) {

            String bankId = transferGroup.getFromAccount().getFullyFormattedNumber();
            List<Transaction> transactions = Lists.newArrayList();

            for (TransferTransactionEntity transferTransaction : transferGroup.getTransactions()) {

                if (shouldParseTransaction(transferTransaction)) {
                    transactions.add(parseTransaction(transferTransaction));
                }
            }

            if (upcomingTransactionsByBankId.containsKey(bankId)) {
                upcomingTransactionsByBankId.get(bankId).addAll(transactions);
            } else {
                upcomingTransactionsByBankId.put(bankId, transactions);
            }
        }
    }

    private boolean shouldParseTransaction(TransferTransactionEntity transferTransaction) {
        // Should not parse if the transactions are already in the normal transaction list.
        return !(Objects.equal(transferTransaction.getTransactionDetails().getDateDependency(), "DIRECT") ||
                Objects.equal(transferTransaction.getTransactionDetails().getStatus(), "UNDER_WAY"));
    }

    private Transaction parseTransaction(TransferTransactionEntity transferTransaction)
        throws Exception {
        Transfer transfer = getTransferForUpcomingPaymentIfUpdatable(transferTransaction,
                Objects.equal(transferTransaction.getType(), "PAYMENT"));

        try {
            return transfer != null ? transferTransaction.toTransaction(transfer) : transferTransaction.toTransaction();
        } catch (IllegalStateException e) {
            log.error("Couldn't parse transaction", e);
        }
        return null;
    }

    /**
     * Create a transfer object for every updatable payment.
     */
    private Transfer getTransferForUpcomingPaymentIfUpdatable(TransferTransactionEntity transferEntity, boolean isPayment)
            throws Exception {
        if (isPayment) {
            ConfirmedInvoice confirmedInvoice = createClientRequest(
                    BASE_URL + transferEntity.getLinks().getSelf().getUri(), false).get(ConfirmedInvoice.class);

            InvoiceDetails invoiceDetails = confirmedInvoice.getTransaction();

            if (invoiceDetails.getLinks() != null
                    && invoiceDetails.getLinks().getEdit() != null) {

                // Only add payments of type EINVOICE for now.
                if (Objects.equal(invoiceDetails.getPayment().getType(), "EINVOICE")) {
                    Transfer transfer = invoiceDetails.toTransfer(true);
                    transfer.setType(TransferType.PAYMENT);
                    return transfer;
                }
            }
        }

        return null;
    }

    private Map<Account, List<TransferDestinationPattern>> getAccountDestinations(ProfileMenu profileMenu, List<Account> updatedAccounts,
            DestinationType destinationType) throws Exception {
        BaseInfoPaymentResponse response = getBaseInfoPaymentResponse(profileMenu);

        if (response == null) {
            return Collections.emptyMap();
        }

        List<GeneralAccountEntity> sourceAccounts = Lists.newArrayList();
        List<GeneralAccountEntity> destinationAccounts = Lists.newArrayList();
        TransferDestinationPatternBuilder builder = new TransferDestinationPatternBuilder();

        switch (destinationType) {
        case TRANSFER:
            sourceAccounts = getSourceAccounts(response.getTransferFromAccounts());
            destinationAccounts.addAll(formatAccountNumbers(response.getAllRecipientAccounts()));
            builder.addMultiMatchPattern(Type.SE, TransferDestinationPattern.ALL);
            break;
        case PAYMENT:
            sourceAccounts = getSourceAccounts(response.getPaymentFromAccounts());
            destinationAccounts.addAll(response.getPayment().getPayees());
            builder.addMultiMatchPattern(Type.SE_PG, TransferDestinationPattern.ALL);
            builder.addMultiMatchPattern(Type.SE_BG, TransferDestinationPattern.ALL);
            break;
        }

        return builder
                .setSourceAccounts(sourceAccounts)
                .setDestinationAccounts(destinationAccounts)
                .setTinkAccounts(updatedAccounts)
                .build();
    }

    private BaseInfoPaymentResponse getBaseInfoPaymentResponse(ProfileMenu profileMenu) {
        MenuItem menuItem = profileMenu.getMenuItem("PaymentBaseinfo");

        if (!menuItem.isAuthorizedURI(MenuItem.Method.GET)) {
            log.info("Unauthorized endpoint: " + menuItem.getUri());
            return null;
        }

        return createClientRequest(BASE_URL + menuItem.getUri(), false).get(BaseInfoPaymentResponse.class);
    }

    private List<GeneralAccountEntity> getSourceAccounts(List<TransactionAccountEntity> fromAccounts) {
        List<GeneralAccountEntity> sourceAccounts = Lists.newArrayList();
        sourceAccounts.addAll(fromAccounts);

        return sourceAccounts;
    }

    /**
     * Nordea person account does not come with a clearing number. So if account number is in form of person number
     * and clearing number is null, we assume it is a Nordea account and put 3300 as clearing number.
     */
    private List<TransactionAccountEntity> formatAccountNumbers(List<TransactionAccountEntity> transactionAccountEntities) {
        for (TransactionAccountEntity accountEntity : transactionAccountEntities) {
            if (accountEntity.getClearingNumber() == null) {
                Sweden personNumber = new SocialSecurityNumber.Sweden(accountEntity.getFullyFormattedNumber());
                if (personNumber.isValid()) {
                    accountEntity.setClearingNumber("3300");
                    accountEntity.setFullyFormattedNumber("3300" + accountEntity.getFullyFormattedNumber());
                }
            }
        }
        return transactionAccountEntities;
    }

    private void updateAccountsAndTransactions(List<AccountEntity> accountEntities, AccountTypes accountType)
            throws Exception {
        Set<AccountTypes> handledAccountTypes = Sets.newHashSet(
                AccountTypes.OTHER,
                AccountTypes.CHECKING,
                AccountTypes.SAVINGS,
                AccountTypes.CREDIT_CARD
        );

        for (AccountEntity accountEntity : accountEntities) {
            if (handledAccountTypes.contains(accountType)) {
                if (!Strings.isNullOrEmpty(accountEntity.getId())) {
                    refreshTransactionalAccount(accountEntity, accountType);
                } else if (!Strings.isNullOrEmpty(accountEntity.getFullyFormattedNumber())) {
                    refreshTransactionLessAccount(accountEntity, accountType);
                }
            }
        }
    }

    private void refreshTransactionalAccount(AccountEntity accountEntity,
            AccountTypes accountType) throws Exception {
        // Construct accounts with as much information as we can possibly get from the accountEntity. We might not be
        // able to get the enriched information for all accounts...

        Optional<Account> optionalAccount = constructTinkAccount(accountEntity, accountType);
        if (!optionalAccount.isPresent()) {
            return;
        }

        Account account = optionalAccount.get();

        ArrayList<Transaction> transactions = Lists.newArrayList();

        // Extract URI for enriched information. Bail early if we can't extract it. (Some accounts don't have it).
        Optional<String> accountTransactionsURI = accountEntity.getTransactionsURI();

        if (!accountTransactionsURI.isPresent()) {
            // If there is no link to fetch transactions and enriched account details...

            // TODO: Investigate if accountEntity.getDetails() links will yield additional details. The current Swedbank
            // app does not seem to use that API endpoint.

            Preconditions.checkState(
                    isValidGenericSwedbankAccountNumber(account.getBankId()),
                    "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

            context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
            context.updateTransactions(account, transactions);

            return;
        }

        // Fetch enriched account details as well as transactions.

        AccountTransactionsResponse accountTransactionsResponse = fetchTransactions(accountTransactionsURI.get());

        // Construct the account from additional information.

        if (account.getType() == AccountTypes.CREDIT_CARD) {
            CardAccountEntity enrichedCardAccountEntity = accountTransactionsResponse.getCardAccount();

            account.setName(enrichedCardAccountEntity.getName());
            account.setBalance(parseAmount(enrichedCardAccountEntity.getCurrentBalance()));
            account.setAvailableCredit(parseAmount(enrichedCardAccountEntity.getAvailableAmount()));
            account.setBankId(enrichedCardAccountEntity.getCardNumber());
            account.setAccountNumber(enrichedCardAccountEntity.getCardNumber());

            Preconditions.checkState(isValidCreditCardAccountNumber(Preconditions.checkNotNull(account.getBankId())),
                    "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());
        } else {
            AccountEntity enrichedAccountEntity = accountTransactionsResponse.getAccount();

            account.setName(enrichedAccountEntity.getName());
            account.setBankId(enrichedAccountEntity.getFullyFormattedNumber());
            account.setAccountNumber(enrichedAccountEntity.getFullyFormattedNumber());

            Preconditions.checkState(
                    isValidNonCreditCardAccountNumber(Preconditions.checkNotNull(account.getBankId())),
                    "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());
        }

        // Add upcoming transactions for this account.

        transactions.addAll(getUpcomingTransactionsForBankId(account.getBankId()));

        // Add pending transactions.

        List<Transaction> pendingTransactions = convertTransactions(accountTransactionsResponse
                .getReservedTransactions());

        for (Transaction pendingTransaction : pendingTransactions) {
            pendingTransaction.setPending(true);
            transactions.add(pendingTransaction);
        }

        // Add transactions from first page.

        transactions.addAll(convertTransactions(accountTransactionsResponse.getTransactions()));

        // Fetch more transactions if they are available and we deem it
        // interesting.
        switch (account.getType()) {
        case CREDIT_CARD:
            pageCreditCardTransactions(account, transactions, accountTransactionsResponse, accountEntity);
            break;
        default:
            pageTransactions(account, transactions, accountTransactionsResponse);
        }

        context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        context.updateTransactions(account, transactions);
    }

    private void pageCreditCardTransactions(Account account, ArrayList<Transaction> transactions,
            AccountTransactionsResponse accountTransactionsResponse, AccountEntity accountEntity) throws Exception {
        int page = 1;
        while (!isContentWithRefresh(account, transactions) &&
                accountTransactionsResponse.isMoreTransactionsAvailable()) {
            page++;
            String url = buildCreditCardTransactionUrl(accountEntity, page);
            accountTransactionsResponse = createClientRequest(url, false).get(AccountTransactionsResponse.class);

            transactions.addAll(convertTransactions(accountTransactionsResponse.getTransactions()));

            context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        }
    }

    private String buildCreditCardTransactionUrl(AccountEntity accountEntity, int page) {
        return UriBuilder.fromUri(BASE_URL_V5 + "/engagement/cardaccount/"
                + accountEntity.getId()).queryParam("transactionsPerPage", 20).queryParam("page", page).build()
                .toString();
    }

    private void pageTransactions(Account account, ArrayList<Transaction> transactions,
            AccountTransactionsResponse accountTransactionsResponse) throws Exception {
        while (!isContentWithRefresh(account, transactions) &&
                accountTransactionsResponse.isMoreTransactionsAvailable() &&
                accountTransactionsResponse.getNumberOfTransactions() > 0 &&
                accountTransactionsResponse.getTransactionsURI().isPresent()) {

            String url = BASE_URL + accountTransactionsResponse.getTransactionsURI().get();
            accountTransactionsResponse = sendRetryableGetRequest(url, AccountTransactionsResponse.class);

            transactions.addAll(convertTransactions(accountTransactionsResponse.getTransactions()));

            context.updateStatus(CredentialsStatus.UPDATING, account, transactions);
        }
    }

    private void refreshTransactionLessAccount(AccountEntity enrichedAccountEntity,
            AccountTypes accountType) {
        Optional<Account> optionalAccount = constructTinkAccount(enrichedAccountEntity, accountType);
        if (!optionalAccount.isPresent()) {
            return;
        }
        Account account = optionalAccount.get();

        AccountFeatures assets = fetchAccountFeatures(account, enrichedAccountEntity);

        Preconditions.checkState(
                isValidGenericSwedbankAccountNumber(account.getBankId()),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        context.cacheAccount(account, assets);
    }

    /**
     * Sign a transfer transaction (either creating a recipient or executing an external transaction).
     */
    private Optional<LinkEntity> signTransfer(LinkEntity signLink)
            throws Exception {
        // Sign the creating using BankID.

        MobileBankIdSignRequest signRequest = new MobileBankIdSignRequest();
        signRequest.setInitiate(true);

        MobileBankIdSignResponse signResponse = createTransferRequest(
                RequestMethod.POST,
                BASE_URL + signLink.getUri(),
                signRequest,
                MobileBankIdSignResponse.class);

        credentials.setSupplementalInformation(null);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        context.requestSupplementalInformation(credentials, false);

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            signResponse = createTransferRequest(
                    RequestMethod.GET,
                    BASE_URL + signResponse.getLinks().getNext().getUri(),
                    MobileBankIdSignResponse.class);

            String status = signResponse.getSigningStatus();
            if (Objects.equal("COMPLETE", status)) {
                return Optional.ofNullable(signResponse.getLinks().getNext());
            } else if (Objects.equal("CANCELLED", status)) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(context.getCatalog()
                                .getString(TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                        .build();
            } else if (Objects.equal("TIMEOUT", status)) {
                break; // Breaks loop and throws TransferExecutionException below.
            }

            log.info("Waiting for BankID completion, status " + status);

            Thread.sleep(2000);
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        context.getCatalog().getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
                .build();

    }

    @Override
    public boolean isLoggedIn() throws Exception {
        return keepAlive();
    }

    @Override
    public boolean keepAlive() throws Exception {

        // No cookies means that we are logged out
        if (client.getClientHandler().getCookieStore().getCookies().size() == 0) {
            return false;
        }

        // Keep the session active by requesting and updating the profile response
        EngagementOverviewResponse engagementOverView = null;
        try {
            if (profilesHandler.getActiveProfile() != null) {
                engagementOverView =
                        fetchEngagementOverviewNoActivation(profilesHandler.getActiveProfile().getProfileMenu());
            }
        } catch (Exception e) {
            return false;
        }

        return engagementOverView != null;
    }

    @Override
    public void persistLoginSession() {
        Session session = new Session();
        session.setCookiesFromClient(client);

        credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {
        Session session = credentials.getPersistentSession(Session.class);

        if (session == null) {
            return;
        }

        addSessionCookiesToClient(client, session);
    }

    @Override
    public void clearLoginSession() {

        credentials.removePersistentSession();
    }

    private ProfilesHandler.BankProfile getAndActivateTransferProfile() {
        ProfilesHandler.BankProfile transferProfile = profilesHandler.getTransferProfile();
        if (!profilesHandler.isActiveProfile(transferProfile)) {
            activateProfile(transferProfile);
        }

        return transferProfile;
    }

    private List<Transfer> getActivatedProfileEInvoices(ProfileMenu profileMenu) throws Exception {
        log.info("Fetching e-invoices from bank.");

        List<Transfer> einvoices = Lists.newArrayList();

        for (InvoiceDetails invoiceDetails : getInvoices(profileMenu)) {

            invoiceDetails.getPayment().setType("EINVOICE");
            Transfer transfer = invoiceDetails.toTransfer(false);

            if (transfer.getDestination().isValid()) {
                einvoices.add(transfer);
            }
        }

        return einvoices;
    }

    @SuppressWarnings("unused")
    private void getPdfDocument(Invoice invoice) throws Exception {
        InvoiceDocument invoiceDocument = createClientRequest(
                BASE_URL + invoice.getDetailDocument().getLinks().getSelf().getUri(), false).get(InvoiceDocument.class);
        Form form = new Form();
        form.add("ticket", invoiceDocument.getData().getTicket());

        String invoicePdf = createClientRequest(invoiceDocument.getUrl(), true).type("Application/pdf").entity(form)
                .post(String.class);
    }

    //TODO: check the error codes of the ErrorResponse and create matching messages.

    /**
     * Attempts to get a detailed error message from the bank. If not present, it takes the more general alternative.
     */
    private String getEndUserMessage(ErrorResponse errorResponse,
            TransferExecutionException.EndUserMessage generalErrorMessage) {
        return errorResponse.getErrorMessage()
                .orElse(context.getCatalog().getString(generalErrorMessage));
    }

    private enum DestinationType {
        TRANSFER, PAYMENT
    }

    private enum RequestMethod {
        GET,
        POST,
        PUT
    }

    private enum UserMessage implements LocalizableEnum {
        WRONG_BANK_SWEDBANK(new LocalizableKey(
                "You do not have any accounts at Swedbank. Use Sparbankerna (Mobile BankID) instead.")),
        WRONG_BANK_SAVINGSBANK(new LocalizableKey(
                "You do not have any accounts at Sparbankerna. Use Swedbank (Mobile BankID) instead.")),
        SWEDBANK_YOUTH(new LocalizableKey(
                "Could not find any engagements in Swedbank. If you have Swedbank Ung, we unfortunately do not support them.")),
        USE_SWEDBANK(new LocalizableKey(
                "Could not find any engagements in Swedbank Ung. Use Swedbank (Mobile BankID) instead.")),
        SAVINGSBANK_YOUTH(new LocalizableKey(
                "Could not find any engagements in Sparbankerna. If you have Sparbankerna Ung, we unfortunately do not support them.")),
        USE_SAVINGSBANK(new LocalizableKey(
                "Could not find any engagements in Sparbankerna Ung. Use Sparbankerna (Mobile BankID) instead.")),
        STRONGER_AUTHENTICATION_NEEDED(new LocalizableKey(
                "In order to add new recipients you need to activate Mobile BankID for extended use. This is done in the Internet bank on the page BankID (found in the tab Tillval)."));

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
