package se.tink.libraries.abnamro.client;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.joda.time.DateTime;
import se.tink.backend.rpc.abnamro.AuthenticationRequest;
import se.tink.backend.rpc.abnamro.SubscriptionActivationRequest;
import se.tink.libraries.abnamro.client.exceptions.AlreadySubscribedCustomerException;
import se.tink.libraries.abnamro.client.exceptions.IcsException;
import se.tink.libraries.abnamro.client.exceptions.IcsRetryableException;
import se.tink.libraries.abnamro.client.exceptions.NonRetailCustomerException;
import se.tink.libraries.abnamro.client.exceptions.SubscriptionException;
import se.tink.libraries.abnamro.client.exceptions.UnderAge16CustomerException;
import se.tink.libraries.abnamro.client.model.ErrorEntity;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountContainerEntity;
import se.tink.libraries.abnamro.client.model.creditcards.CreditCardAccountResponse;
import se.tink.libraries.abnamro.client.rpc.CreateSubscriptionRequest;
import se.tink.libraries.abnamro.client.rpc.CreateSubscriptionResponse;
import se.tink.libraries.abnamro.client.rpc.ErrorResponse;
import se.tink.libraries.abnamro.client.rpc.PfmContractResponse;
import se.tink.libraries.abnamro.client.rpc.SubscriptionAccountsRequest;
import se.tink.libraries.abnamro.client.rpc.SubscriptionAccountsResponse;
import se.tink.libraries.abnamro.client.rpc.SubscriptionResponse;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroInternetBankingConfiguration;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * Service client for handling with grip subscriptions.
 *
 */
public class IBSubscriptionClient extends IBClient {

    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Integer ACCOUNT_REQUEST_LIMIT = 20;
    private static final MetricId IB_SUBSCRIPTION_CLIENT_TIMERS_METRIC = MetricId
            .newId("ib_subscription_client_timers");
    private final MetricRegistry metricRegistry;
    private final Counter rejectedContracts;

    private AbnAmroInternetBankingConfiguration configuration;

    public IBSubscriptionClient(AbnAmroConfiguration abnAmroConfiguration, MetricRegistry metricRegistry) {
        super(IBSubscriptionClient.class, abnAmroConfiguration, metricRegistry);

        this.configuration = abnAmroConfiguration.getInternetBankingConfiguration();

        this.metricRegistry = metricRegistry;
        rejectedContracts = metricRegistry.meter(MetricId.newId("ib_subscription_client_timers_rejected_contracts"));
    }

    private Timer getTimer(String source, String outcome) {
        return metricRegistry.timer(IB_SUBSCRIPTION_CLIENT_TIMERS_METRIC
                .label("source", source)
                .label("outcome", outcome));
    }

    /**
     * Initialize a subscription request against the ABN AMRO backend.
     * <p/>
     * Exception AlreadySubscribedCustomerException is thrown if the user already have an active/signed subscription.
     * <p/>
     * Exception NonRetailCustomerException is thrown if a non retail customer tried to subscribe.
     * <p/>
     * Multiple calls for the same customer will then return the same subscriptionId. The subscriptionId is needed
     * when signing the Terms&Conditions.
     */
    public Long subscribe(AuthenticationRequest request) throws SubscriptionException {

        CreateSubscriptionRequest createSubscriptionRequest = new CreateSubscriptionRequest(request.getBcNumber());

        final Stopwatch watch = Stopwatch.createStarted();
        ClientResponse clientResponse = new IBClientRequestBuilder("/pfmsubscription")
                .withSession(request.getSessionToken())
                .build()
                .post(ClientResponse.class, createSubscriptionRequest);
        watch.stop();

        validateContentType(clientResponse, MediaType.APPLICATION_JSON_TYPE);

        CreateSubscriptionResponse response = clientResponse.getEntity(CreateSubscriptionResponse.class);

        if (response.isSuccess()) {
            log.info(String.format("Subscribed (Customer = %s, SubscriptionId = %d)", request.getBcNumber(),
                    response.getId()));
            getTimer("subscribe", "success")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            return response.getId();
        }

        if (response.isCustomerAlreadySubscribed()) {
            log.info(String.format("Already subscribed (Customer = '%s')", request.getBcNumber()));
            getTimer("subscribe", "already_subscribed")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            throw new AlreadySubscribedCustomerException();
        }

        if (response.isNonRetailCustomer()) {
            log.info(String.format("Non retail customer (Customer = '%s')", request.getBcNumber()));
            getTimer("subscribe", "non_retail_customer")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            throw new NonRetailCustomerException();
        }

        if (response.isUnderAge16()) {
            log.info(String.format("Customer under 16 years old (Customer = '%s')", request.getBcNumber()));
            getTimer("subscribe", "under_16_years_old")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            throw new UnderAge16CustomerException();
        }

        getTimer("subscribe", "other_error")
                .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        log.error(String.format(
                "Could not subscribe customer (Reason = '%s', Customer = '%s', HttpStatus = '%d', Errors = '%s')",
                response.getReason().orElse("N/A"), request.getBcNumber(), clientResponse.getStatus(),
                response.getErrorDetails()));

        throw new SubscriptionException();
    }

    /**
     * Verify if the user has an active subscription at the ABN AMRO Backend.
     * <p/>
     * NOT a good solution/design to do the subscribe call but there is no other option for checking if the user has
     * an active subscription. There is no side effects of initializing a subscription since it must be signed to
     * become active.
     */
    public boolean isSubscriptionActivated(SubscriptionActivationRequest request) {

        try {
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            authenticationRequest.setBcNumber(request.getBcNumber());
            authenticationRequest.setSessionToken(request.getSessionToken());

            subscribe(authenticationRequest);
        } catch (AlreadySubscribedCustomerException exception) {
            return true;
        } catch (SubscriptionException e) {
            return false;
        }

        return false;
    }

    /**
     * Version 2 of the subscription endpoint does not require a session token (Used by Grip > 3.0).
     */
    public List<RejectedContractEntity> subscribeAccountsWithoutSession(SubscriptionAccountsRequest subscriptionRequest)
            throws SubscriptionException {
        return subscribeAccounts("/pfmsubscription/v2", subscriptionRequest, null);
    }

    /**
     * Version 1 of the subscription endpoint requires a session token (Used by Grip < 3.0). `null` can be changed to
     * `v1` when the `v2` version is deployed to ABN AMRO production.
     */
    public List<RejectedContractEntity> subscribeAccountsWithSession(String sessionToken,
            SubscriptionAccountsRequest subscriptionRequest) throws SubscriptionException {
        return subscribeAccounts("/pfmsubscription", subscriptionRequest, sessionToken);
    }

    /**
     * Subscribe a list of accounts/contracts towards ABN AMRO. Subscription needs to be activated/signed before
     * this call can be made.
     * <p>
     * Calls are paged since ABN AMRO has a limit on 20 accounts per request.
     * <p>
     * @return False if there was an error in the request and true if the request was a success. Will return true even
     * if one or several accounts got rejected.
     */
    private List<RejectedContractEntity> subscribeAccounts(String url, SubscriptionAccountsRequest subscriptionRequest,
            String sessionToken) throws SubscriptionException {

        List<List<Long>> partitions = Lists.partition(subscriptionRequest.getContracts(), ACCOUNT_REQUEST_LIMIT);

        List<RejectedContractEntity> rejectedContracts = Lists.newArrayList();

        for (List<Long> partition : partitions) {

            SubscriptionAccountsRequest request = new SubscriptionAccountsRequest();
            request.setBcNumber(subscriptionRequest.getBcNumber());
            request.setContracts(partition);

            final Stopwatch watch = Stopwatch.createStarted();
            ClientResponse clientResponse = new IBClientRequestBuilder(url)
                    .withSession(sessionToken)
                    .build()
                    .put(ClientResponse.class, request);
            watch.stop();

            validateContentType(clientResponse, MediaType.APPLICATION_JSON_TYPE);

            SubscriptionAccountsResponse response = clientResponse.getEntity(SubscriptionAccountsResponse.class);

            if (response.isError()) {
                log.error(String.format(
                        "Could not subscribe contracts (Customer = %s, Contracts = %s, HttpStatus = %d, Errors = %s)",
                        request.getBcNumber(), SerializationUtils.serializeToString(partition),
                        clientResponse.getStatus(), response.getErrorDetails()));
                getTimer("subscribeAccounts", "unable_to_subscribe")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                throw new SubscriptionException("Could not subscribe accounts");
            }

            int numberOfRejectedContracts = response.getNumberOfRejectedContracts();

            log.info(String.format("Subscribed (Customer = %s, Contracts = %d, Rejected = %d)", request.getBcNumber(),
                    request.getContracts().size(), numberOfRejectedContracts));
            getTimer("subscribeAccounts", "success")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

            for (Long contract : request.getContracts()) {
                log.debug(String.format("Subscribed (Contract = %d)", contract));
            }

            for (RejectedContractEntity rejected : response.getRejectedContracts()) {
                log.error(
                        String.format("Rejected contract (Customer = %s, Contract = %s, ReasonCode = %s, Reason = %s)",
                                request.getBcNumber(), rejected.getContractNumber(),
                                rejected.getRejectedReasonCode(), rejected.getRejectionReason()));
            }

            // Keep track of the rejected contracts
            rejectedContracts.addAll(response.getRejectedContracts());

        }

        this.rejectedContracts.inc(rejectedContracts.size());
        return rejectedContracts;
    }

    /**
     * Unsubscribe a customer against ABN AMRO. Unsubscribe means that real-time transactions won't be pushed to Tink.
     *
     * @return True/False depending on if the customer was unsubscribed or not.
     */
    public boolean unsubscribe(String bcNumber) {

        String url = String.format("/pfmsubscription/%s", bcNumber);

        final Stopwatch watch = Stopwatch.createStarted();
        ClientResponse clientResponse = new IBClientRequestBuilder(url).build().delete(ClientResponse.class);
        watch.stop();

        int statusCode = clientResponse.getStatus();

        if (statusCode == HttpStatusCodes.STATUS_CODE_OK || statusCode == HttpStatusCodes.STATUS_CODE_NO_CONTENT) {
            log.info(String.format("Unsubscribed (Customer = %s)", bcNumber));
            clientResponse.close();
            getTimer("unsubscribe", "success")
                    .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            return true;
        }

        ErrorResponse response = clientResponse.getEntity(ErrorResponse.class);

        log.error(String.format("Could not unsubscribe (Customer = %s, HttpStatus = %d, Errors = %s",
                bcNumber, clientResponse.getStatus(), response.getErrorDetails()));
        getTimer("unsubscribe", "error")
                .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        return false;
    }

    /**
     * Resync transaction history 18 months back. Transactions will be pushed back through the connector.
     *
     * @param accounts which accounts to resync
     * @return true/false if the resync was initiated correctly against ABN AMRO
     */
    public boolean resync(List<Long> accounts) throws SubscriptionException {

        // + 1 day is needed since dates earlier or equal to 18 months back fails at ABN AMRO
        DateTime from = new DateTime().minusMonths(18).plusDays(1);
        DateTime to = new DateTime();

        return resync(accounts, from, to);
    }

    /**
     * Initiate a resync request against ABN AMRO. Transactions will be pushed back through the connector.
     *
     * Calls are paged since ABN AMRO has a limit on 20 accounts per request.
     *
     * Business logic at ABN AMRO
     * - ABN AMRO accepts empty from and to but are then returning last 30 days transactions.
     * - Leaving one of `from` or `to` null means that you only get transactions for that date that you provided.
     *
     * @param accounts which accounts to resync
     * @param from     which date the resync should start at
     * @param to       which date the resync should end at
     * @return true/false if the resync was initiated at ABN AMRO
     */
    public boolean resync(List<Long> accounts, DateTime from, DateTime to) throws SubscriptionException {

        Preconditions.checkNotNull(accounts, "Accounts can't be null");
        Preconditions.checkArgument(accounts.size() > 0, "Accounts cant be empty");
        Preconditions.checkNotNull(from, "From date must be provided");
        Preconditions.checkNotNull(to, "To date must be provided");

        List<List<Long>> partitions = Lists.partition(accounts, ACCOUNT_REQUEST_LIMIT);

        for (List<Long> partition : partitions) {

            String accountsParam = COMMA_JOINER.join(partition);

            String url = UriBuilder.fromPath("/pfmresync")
                    .queryParam("accounts", accountsParam)
                    .queryParam("fromDate", ThreadSafeDateFormat.FORMATTER_DAILY.format(from.toDate()))
                    .queryParam("toDate", ThreadSafeDateFormat.FORMATTER_DAILY.format(to.toDate()))
                    .build()
                    .toString();

            ClientResponse clientResponse = new IBClientRequestBuilder(url).build().get(ClientResponse.class);

            if (HttpStatusCodes.isSuccess(clientResponse.getStatus())) {
                log.info(
                        String.format("Resync initiated (Accounts = %s, From = %s, To = %s)", accountsParam, from, to));
                continue;
            }

            validateContentType(clientResponse, MediaType.APPLICATION_JSON_TYPE);

            ErrorResponse response = clientResponse.getEntity(ErrorResponse.class);

            log.error(String.format("Resync failed (Accounts = %s, HttpStatus = %d, Errors = %s", accountsParam,
                    clientResponse.getStatus(), response.getErrorDetails()));

            return false;
        }

        return true;
    }

    public Optional<SubscriptionResponse> getSubscription(String bcNumber) throws SubscriptionException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(bcNumber));

        String url = String.format("/pfmsubscription/%s", bcNumber);

        ClientResponse response = new IBClientRequestBuilder(url).build().get(ClientResponse.class);

        validateContentType(response, MediaType.APPLICATION_JSON_TYPE);

        SubscriptionResponse subscriptionResponse = response.getEntity(SubscriptionResponse.class);

        if (Strings.isNullOrEmpty(subscriptionResponse.getBcNumber())) {
            log.error(String.format(
                    "Could not retrieve subscription details (Customer = %s, HttpStatus = %d, Errors = %s",
                    bcNumber, response.getStatus(), subscriptionResponse.getErrorDetails()));

            return Optional.empty();
        }

        return Optional.of(subscriptionResponse);
    }

    public List<PfmContractEntity> getContracts(String bcNumber) throws SubscriptionException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(bcNumber), "BcNumber must not be null or empty");

        String url = String.format("/pfmsubscription/%s/contracts", bcNumber);

        ClientResponse response = new IBClientRequestBuilder(url).build().get(ClientResponse.class);

        validateContentType(response, MediaType.APPLICATION_JSON_TYPE);

        PfmContractResponse entity = response.getEntity(PfmContractResponse.class);

        return Lists.newArrayList(entity);
    }

    /**
     * This will return credit card information and credit card transactions for the specified user and contract number.
     */
    public List<CreditCardAccountContainerEntity> getCreditCardAccountAndTransactions(String bcNumber, Long contract)
            throws IcsException {
        String url = UriBuilder.fromPath("/creditcardaccounts/pfm")
                .queryParam("bcNumber", bcNumber)
                .queryParam("contractNumbers", contract)
                .build()
                .toString();

        ClientResponse clientResponse = new IBClientRequestBuilder(url).build().get(ClientResponse.class);

        try {
            if (clientResponse.getStatus() == HttpStatusCodes.STATUS_CODE_OK) {
                CreditCardAccountResponse response = clientResponse.getEntity(CreditCardAccountResponse.class);
                return response.getCreditCardAccountList().getCreditCardAccounts();
            }

            if (Objects.equals(clientResponse.getType(), MediaType.APPLICATION_JSON_TYPE)) {
                ErrorResponse response = clientResponse.getEntity(ErrorResponse.class);

                if (!response.getMessages().isEmpty()) {
                    ErrorEntity error = response.getMessages().get(0);

                    // The VPN connection between ABN AMRO and ICS is unstable so throw retryable exception if possible
                    if (error.isRetryable()) {
                        log.warn(String.format("Retryable error from ABN AMRO ICS (Key = '%s', Reason = '%s')",
                                error.getMessageKey(), error.getReason()));
                        throw new IcsRetryableException(error.getMessageKey(), error.getReason());
                    }

                    throw new IcsException(error.getMessageKey(), error.getReason());
                }
            }

            throw new IcsException(
                    String.format("Could not fetch transactions. HTTP status: %d", clientResponse.getStatus()));
        } finally {
            clientResponse.close();
        }
    }

    /**
     * Have asked ABN AMRO to use "Token" instead of "Basic" /Erik
     */
    @Override
    protected WebResource.Builder createClientRequest(String path) {
        return super.createClientRequest(path)
                .header(HttpHeaders.AUTHORIZATION, String.format("Basic %s", configuration.getAuthorizationToken()));
    }

    private void validateContentType(ClientResponse response, MediaType expected) throws SubscriptionException {
        if (Objects.equals(response.getType(), expected)) {
            return;
        }

        log.error(String.format(
                "Unexpected content type (Status = '%d', Expected = '%s', Received = '%s')",
                response.getStatus(), expected, response.getType()));

        throw new SubscriptionException("Unexpected content type on response.");
    }

}
