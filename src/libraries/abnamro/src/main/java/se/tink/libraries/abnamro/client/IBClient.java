package se.tink.libraries.abnamro.client;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import se.tink.backend.core.Account;
import se.tink.libraries.enums.AccountTypes;
import se.tink.libraries.abnamro.client.rpc.AuthenticatedRequest;
import se.tink.libraries.abnamro.client.rpc.AuthenticationRequest;
import se.tink.libraries.abnamro.client.exceptions.InternetBankingUnavailableException;
import se.tink.libraries.abnamro.client.exceptions.UnauthorizedAccessException;
import se.tink.libraries.abnamro.client.model.ContractContainer;
import se.tink.libraries.abnamro.client.model.ContractEntity;
import se.tink.libraries.abnamro.client.model.ErrorEntity;
import se.tink.libraries.abnamro.client.model.ProductEntity;
import se.tink.libraries.abnamro.client.model.SessionEntity;
import se.tink.libraries.abnamro.client.rpc.ContractsResponse;
import se.tink.libraries.abnamro.client.rpc.ErrorResponse;
import se.tink.libraries.abnamro.client.rpc.SessionResponse;
import se.tink.libraries.abnamro.client.rpc.UserPreferenceResponse;
import se.tink.libraries.abnamro.client.rpc.UserPreferences;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroProductConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroProductsConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * In the context of ABN AMRO, the "IB" prefix stands for Internet Banking. The IB endpoints are the same as they use
 * for their Internet banking services, such as on the web or in their app.
 */
public class IBClient extends Client {

    private static final String SERVICE_VERSION_HEADER = "x-aab-serviceversion";
    private static final String SESSION_COOKIE_NAME = "SMSession";
    private final static Joiner COMMA_JOINER = Joiner.on(",").skipNulls();
    private static final String DEFAULT_LANGUAGE = "en";
    private static final MetricId AUTHENTICATION_OUTCOME_METRIC = MetricId.newId("ib_client_authentication_outcome");

    private final AbnAmroProductsConfiguration productsConfiguration;
    private final Counter authenticationErrors;
    private final MetricRegistry metricRegistry;

    private static class USER_PREFERENCE_IDS {
        private static String CREDIT_CARDS = "creditCards";
    }

    private static class USER_PREFERENCE_FIELDS {
        private static String APPROVED_CREDIT_CARDS = "hasGivenApprovalCreditCards";
    }

    public IBClient(AbnAmroConfiguration abnAmroConfiguration, MetricRegistry metricRegistry) {
        this(IBClient.class, abnAmroConfiguration, metricRegistry);
    }

    protected IBClient(Class<? extends Client> cls, AbnAmroConfiguration abnAmroConfiguration, MetricRegistry
            metricRegistry) {
        super(cls, abnAmroConfiguration.getTrustStoreConfiguration(),
                abnAmroConfiguration.getInternetBankingConfiguration().getHost());

        this.productsConfiguration = abnAmroConfiguration.getInternetBankingConfiguration().getProducts();

        this.metricRegistry = metricRegistry;
        this.authenticationErrors = metricRegistry.meter(MetricId.newId("ib_client_authenticate_errors"));
    }

    private Timer getTimer(String source, String outcome) {
        return metricRegistry.timer(AUTHENTICATION_OUTCOME_METRIC
                .label("source", source)
                .label("outcome", outcome));
    }

    public boolean authenticate(AuthenticationRequest request) {
        if (Strings.isNullOrEmpty(request.getBcNumber())) {
            log.error("Invalid request: missing bc number.");
            return false;
        }

        try {
            final Stopwatch watch = Stopwatch.createStarted();

            Optional<String> bcNumber = getCustomerNumber(request.getSessionToken());

            watch.stop();

            // No BC number in response, hence nothing to compare.
            if (!bcNumber.isPresent()) {
                log.error(String.format("Invalid response data for %s.", request.getBcNumber()));
                getTimer("authenticate", "no_bc_number")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return false;
            }

            if (request.getBcNumber().equals(bcNumber.get())) {
                log.debug(String.format("Successfully authenticated %s.", request.getBcNumber()));
                getTimer("authenticate", "success")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return true;
            } else {
                log.error(String.format("Customer numbers don't match. Requested %s but received %s.",
                        request.getBcNumber(), bcNumber.get()));
                getTimer("authenticate", "customer_mismatch")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return false;
            }
        } catch (Exception e) {
            authenticationErrors.inc();
            log.error(String.format("Failed to authenticate %s.", request.getBcNumber()), e);
            return false;
        }
    }

    public Optional<String> getCustomerNumber(String sessionToken)
            throws InternetBankingUnavailableException, UnauthorizedAccessException {
        ClientResponse sessionClientResponse = new IBClientRequestBuilder("/session")
                .withServiceVersion("v3")
                .withSession(sessionToken)
                .build()
                .get(ClientResponse.class);

        if (sessionClientResponse.getStatus() == Status.OK.getStatusCode()) {
            SessionResponse sessionResponse = sessionClientResponse.getEntity(SessionResponse.class);
            SessionEntity session = sessionResponse.getSession();

            String bcNumber = session.getRepresentedCustomer();

            // This logic does not match the implementation on the clients and all sessions should have a
            // represented customer. Will remove the below code when we are sure it isn't used. /Erik
            if (Strings.isNullOrEmpty(bcNumber)) {
                log.warn("Could not retrieve 'represented customer'. Fallback to 'selected customer'.");
                bcNumber = session.getSelectedCustomer();
            }

            return Optional.ofNullable(bcNumber);
        } else if (sessionClientResponse.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            throw new UnauthorizedAccessException();
        } else {
            ErrorResponse errorResponse = null;

            if (hasValidContentType(sessionClientResponse, MediaType.APPLICATION_JSON_TYPE)) {
                errorResponse = sessionClientResponse.getEntity(ErrorResponse.class);
            } else {
                sessionClientResponse.close();
            }

            String status = getStatusMessage(errorResponse);

            throw new InternetBankingUnavailableException(
                    String.format("Could not call session service (Status = '%s', Message = '%s').",
                    sessionClientResponse.getStatus(), status));
        }
    }

    public List<Account> getAccounts(AuthenticatedRequest request, String language, boolean cleanBankId) {

        AbnAmroProductConfiguration savingProducts = productsConfiguration.getSavingProducts();
        AbnAmroProductConfiguration paymentProducts = productsConfiguration.getPaymentProducts();
        AbnAmroProductConfiguration creditCardProducts = productsConfiguration.getCreditCardProducts();

        Set<Integer> productIds = Sets.newHashSet(savingProducts.getIds());
        Set<String> productGroups = Sets.newHashSet(savingProducts.getGroups());

        productIds.addAll(paymentProducts.getIds());
        productGroups.addAll(paymentProducts.getGroups());

        // Only request credit cards if the user has enabled credit cards in Mobile Banking
        if (hasEnabledCreditCards(request)) {
            productIds.addAll(creditCardProducts.getIds());
            productGroups.addAll(creditCardProducts.getGroups());
        }

        String url = UriBuilder.fromPath("/contracts")
                .queryParam("bcNumber", request.getBcNumber())

                // Query filter to only return contracts owned by the customer. Used so that we don't include accounts
                // that the customer has access to but doesn't owe (requirement from legal at ABN).
                .queryParam("includeBCJoiningType", "PRIVATE_CJ")

                // Filter only the products building blocks
                .queryParam("productBuildingBlocks", COMMA_JOINER.join(productIds))

                // Filter product groups
                .queryParam("productGroups", COMMA_JOINER.join(productGroups))
                .build()
                .toString();

        final Stopwatch watch = Stopwatch.createStarted();
        ClientResponse clientResponse = new IBClientRequestBuilder(url)
                .withServiceVersion("v2")
                .withSession(request.getSessionToken())
                .withLanguage(language)
                .build()
                .get(ClientResponse.class);
        watch.stop();

        if (!hasValidContentType(clientResponse, MediaType.APPLICATION_JSON_TYPE)) {
            clientResponse.close();
            getTimer("getAccounts", "invalid_content_type")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            return Collections.emptyList();
        }

        ContractsResponse contractsResponse = clientResponse.getEntity(ContractsResponse.class);

        if (contractsResponse.isError()) {
            log.error(String.format("Could not get accounts (HttpStatus = %d, Errors = %s",
                    clientResponse.getStatus(), contractsResponse.getErrorDetails()));
            getTimer("getAccounts", "accounts_missing")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            return Collections.emptyList();
        }

        getTimer("getAccounts", "success")
                .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        return contractsResponse.getContractList().stream()
                .map(ContractContainer::getContract)
                .filter(AbnAmroUtils.Predicates.IS_VALID_CONTRACT_ENTITY::apply)
                .distinct() // to only include unique contracts
                .map(IBClient::getAccount)
                .collect(Collectors.toList());
    }

    /**
     * Check if customer has enabled credit card in Mobile Banking. Enabled mean means that we can collect credit cards
     * transactions. Failure of collecting the information will return false.
     */
    public boolean hasEnabledCreditCards(AuthenticatedRequest request) {

        UserPreferences preferences = getUserPreferences(USER_PREFERENCE_IDS.CREDIT_CARDS, request.getSessionToken());

        if (preferences == null) {
            log.error(String.format("Could not get credit card preferences (Customer = '%s')", request.getBcNumber()));
            return false;
        }

        Optional<Boolean> approved = preferences.getBoolean(USER_PREFERENCE_FIELDS.APPROVED_CREDIT_CARDS);

        // User preferences aren't available if the user hasn't used Mobile Banking.
        if (!approved.isPresent()) {
            log.info(String.format("Did not find approval field (Customer = '%s', Field = '%s')",
                    request.getBcNumber(), USER_PREFERENCE_FIELDS.APPROVED_CREDIT_CARDS));
            return false;
        }

        log.info(String.format("Retrieved credit card preferences (Customer = '%s', Approved = '%s')",
                request.getBcNumber(), approved.get()));

        return approved.get();
    }

    private UserPreferences getUserPreferences(String id, String sessionToken) {
        List<UserPreferences> preferences = getUserPreferences(ImmutableSet.of(id), sessionToken);

        if (preferences != null && preferences.size() > 0) {
            return preferences.get(0);
        }

        return null;
    }

    private List<UserPreferences> getUserPreferences(Set<String> ids, String sessionToken) {

        String url = UriBuilder.fromPath("/user/preferences")
                .queryParam("ids", COMMA_JOINER.join(ids))
                .build()
                .toString();

        final Stopwatch watch = Stopwatch.createStarted();
        ClientResponse clientResponse = new IBClientRequestBuilder(url)
                .withSession(sessionToken)
                .build()
                .get(ClientResponse.class);
        watch.stop();

        if (!hasValidContentType(clientResponse, MediaType.APPLICATION_JSON_TYPE)) {
            getTimer("getUserPreferences", "invalid_content_type")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            clientResponse.close();
            return null;
        }

        UserPreferenceResponse response = clientResponse.getEntity(UserPreferenceResponse.class);

        if (response.isError()) {
            log.error(String.format("Could not get user preferences (HttpStatus = %d, Errors = %s",
                    clientResponse.getStatus(), response.getErrorDetails()));
            getTimer("getUserPreferences", "user_preferences_missing")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            return null;
        }

        getTimer("getUserPreferences", "success")
                .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        return response.getUserPreferenceList().getUserPreferences();
    }

    private static Account getAccount(ContractEntity contractEntity) {
        Account account = new Account();

        String accountNumber = contractEntity.getContractNumber();

        // Remove leading zeros
        if (accountNumber != null) {
            accountNumber = accountNumber.replaceFirst("^0+(?!$)", "");
        }

        account.setBankId(accountNumber);

        account.setType(getAccountType(contractEntity.getProduct()));

        if (Objects.equals(account.getType(), AccountTypes.CREDIT_CARD)) {
            account.setName(contractEntity.getProduct().getName().replaceFirst("ABN AMRO\\s*", ""));
        } else {
            account.setName(contractEntity.getProduct().getName());
        }

        Map<String, String> payload = Maps.newHashMap();

        if (contractEntity.getBalance() != null) {
            account.setBalance(contractEntity.getBalance().getAmount());
            payload.put(AbnAmroUtils.InternalAccountPayloadKeys.CURRENCY,
                    contractEntity.getBalance().getCurrencyCode());
        }

        if (Strings.isNullOrEmpty(contractEntity.getAccountNumber())) {
            account.setAccountNumber(account.getBankId());
            payload.put(AbnAmroUtils.InternalAccountPayloadKeys.IBAN, contractEntity.getAccountNumber());
        } else {
            account.setAccountNumber(AbnAmroUtils.prettyFormatIban(contractEntity.getAccountNumber()));
        }

        if (!payload.isEmpty()) {
            account.setPayload(SerializationUtils.serializeToString(payload));
        }

        return account;
    }

    private static AccountTypes getAccountType(ProductEntity product) {
        AccountTypes type = AbnAmroUtils.getAccountType(product.getProductGroup());
        if (type == AccountTypes.CHECKING) {
            if (product.isCreditAcccount()) {
                return AccountTypes.CREDIT_CARD;
            } else {
                return AccountTypes.CHECKING;
            }
        } else {
            return type;
        }
    }

    private static String getStatusMessage(ErrorResponse errorResponse) {
        if (errorResponse == null) {
            return null;
        }

        if (errorResponse.getMessages() == null || errorResponse.getMessages().isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (ErrorEntity me : errorResponse.getMessages()) {
            sb.append("type:");
            sb.append(me.getMessageType());
            sb.append(", ");
            sb.append("key:");
            sb.append(me.getMessageKey());
            sb.append(", ");
            sb.append("text:");
            sb.append(me.getMessageText());
        }

        return sb.toString();
    }

    private boolean hasValidContentType(ClientResponse response, MediaType expected) {

        if (Objects.equals(response.getType(), expected)) {
            return true;
        }

        log.error(String.format("Unexpected content type(Expected = '%s', Received = '%s')",
                expected, response.getType()));

        return false;
    }

    public class IBClientRequestBuilder {

        private Builder builder;
        private String sessionToken;
        private String language;
        private String serviceVersion;

        IBClientRequestBuilder(String path) {
            this.builder = createClientRequest(path);
        }

        IBClientRequestBuilder withSession(String sessionToken) {
            this.sessionToken = sessionToken;
            return this;
        }
        
        IBClientRequestBuilder withLanguage(String language) {
            this.language = language;
            return this;
        }

        IBClientRequestBuilder withServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        Builder build() {
            builder = builder.header("Accept-Language", Strings.isNullOrEmpty(language) ? DEFAULT_LANGUAGE : language);

            if (!Strings.isNullOrEmpty(serviceVersion)) {
                builder = builder.header(SERVICE_VERSION_HEADER, serviceVersion);
            }

            if (!Strings.isNullOrEmpty(sessionToken)) {
                builder = builder.cookie(new NewCookie(SESSION_COOKIE_NAME, sessionToken));
            }

            return builder;
        }
    }
}
