package se.tink.backend.main.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.abnamro.utils.AbnAmroTestDataUtils;
import se.tink.backend.api.AbnAmroService;
import se.tink.backend.api.UserService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroSubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.tracking.TrackableEvent;
import se.tink.backend.core.AbnAmroBufferedAccount;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.main.controllers.MarketServiceController;
import se.tink.backend.main.controllers.abnamro.AbnAmroAccountController;
import se.tink.backend.main.controllers.abnamro.AbnAmroCreditCardController;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.backend.rpc.abnamro.AbnAmroUserLoginResponse;
import se.tink.backend.rpc.abnamro.AccountSubscriptionRequest;
import se.tink.backend.rpc.abnamro.AuthenticatedRequest;
import se.tink.backend.rpc.abnamro.AuthenticationRequest;
import se.tink.backend.rpc.abnamro.CustomerValidationRequest;
import se.tink.backend.rpc.abnamro.SubscriptionActivationRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.api.headers.TinkHttpHeaders;
import se.tink.libraries.abnamro.client.IBClient;
import se.tink.libraries.abnamro.client.IBSubscriptionClient;
import se.tink.libraries.abnamro.client.exceptions.AlreadySubscribedCustomerException;
import se.tink.libraries.abnamro.client.exceptions.NonRetailCustomerException;
import se.tink.libraries.abnamro.client.exceptions.SubscriptionException;
import se.tink.libraries.abnamro.client.exceptions.UnderAge16CustomerException;
import se.tink.libraries.abnamro.client.model.RejectedContractEntity;
import se.tink.libraries.abnamro.client.model.SubscriptionResult;
import se.tink.libraries.abnamro.client.rpc.SubscriptionAccountsRequest;
import se.tink.libraries.abnamro.config.AbnAmroAccountUpdatesConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.abnamro.utils.AbnAmroAccountsUpdateChecker;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.abnamro.utils.SubscriptionResultBuilder;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static com.google.common.base.Objects.equal;

public class AbnAmroServiceResource implements AbnAmroService {

    private final MetricRegistry metricRegistry;
    @Context
    private HttpServletRequest request;

    private final ServiceContext serviceContext;
    private final SystemServiceFactory systemServiceFactory;
    private final MarketServiceController marketServiceController;

    private final AbnAmroSubscriptionRepository abnAmroSubscriptionRepository;
    private final AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;

    private final AbnAmroConfiguration configuration;
    private final HttpResponseHelper httpResponseHelper;
    private static final LogUtils log = new LogUtils(AbnAmroServiceResource.class);
    private final AbnAmroCreditCardController abnAmroCreditCardController;
    private final AbnAmroAccountController abnAmroAccountController;
    private final ClientValidator clientValidator;
    private final EventTracker tracker;

    public AbnAmroServiceResource(MetricRegistry metricRegistry,
            final ServiceContext serviceContext) {
        this.metricRegistry = metricRegistry;
        this.serviceContext = serviceContext;
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();
        MarketProvider marketProvider = new MarketProvider(
                serviceContext.getRepository(MarketRepository.class),
                serviceContext.getRepository(CurrencyRepository.class),
                serviceContext.getConfiguration().getAuthentication());

        this.marketServiceController = new MarketServiceController(marketProvider);

        this.abnAmroAccountController = new AbnAmroAccountController(serviceContext.getConfiguration().getCluster(),
                serviceContext.getSystemServiceFactory());

        this.configuration = serviceContext.getConfiguration().getAbnAmro();
        this.abnAmroCreditCardController = new AbnAmroCreditCardController(
                serviceContext.getConfiguration().getCluster(),
                serviceContext.getRepository(CredentialsRepository.class),
                new CredentialsRequestRunnableFactory(serviceContext), abnAmroAccountController,
                () -> serviceContext, configuration);

        this.httpResponseHelper = new HttpResponseHelper(log);
        this.abnAmroSubscriptionRepository = serviceContext.getRepository(AbnAmroSubscriptionRepository.class);
        this.abnAmroBufferedAccountRepository = serviceContext.getRepository(AbnAmroBufferedAccountRepository.class);
        this.accountRepository = serviceContext.getRepository(AccountRepository.class);
        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.userRepository = serviceContext.getRepository(UserRepository.class);
        this.userStateRepository = serviceContext.getRepository(UserStateRepository.class);

        this.clientValidator = new ClientValidator(new ClientProvider(serviceContext.getConfiguration().getCluster()),
                marketProvider);
        this.tracker = serviceContext.getEventTracker();
    }

    @Override
    public AbnAmroUserLoginResponse authenticate(AuthenticationRequest authenticationRequest) {

        // Validate the client _before_ doing anything else.
        clientValidator
                .validateClient(request != null ? request.getHeader(TinkHttpHeaders.CLIENT_KEY_HEADER_NAME) : null,
                        request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

        validate(authenticationRequest);

        log.debug(String.format("Authenticating %s.", authenticationRequest.getBcNumber()));

        // Authenticate the user towards ABN AMRO.
        IBClient ibClient = null;

        try {
            ibClient = new IBClient(configuration, metricRegistry);
        } catch (Exception e) {
            httpResponseHelper.error(Status.INTERNAL_SERVER_ERROR, "Unable to set up secure client.", e);
        }

        if (!ibClient.authenticate(authenticationRequest)) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        final String username = AbnAmroLegacyUserUtils.getUsername(authenticationRequest);
        final User user = userRepository.findOneByUsername(username);

        UserLoginResponse response;
        Optional<Date> lastLogin = Optional.empty();

        // No user exists for the customer. Create it!
        if (user == null) {
            response = register(authenticationRequest);
        }
        // The user already exists. Log in!
        else {
            // Get the user last login before logging in again
            UserState userState = userStateRepository.findOneByUserId(user.getId());

            if (userState != null) {
                lastLogin = Optional.ofNullable(userState.getLastLogin());
            }

            response = login(user);
        }

        return createAbnAmroUserLoginResponse(response, authenticationRequest, lastLogin);
    }

    /**
     * Return the existing subscription or create a new one if it doesn't exist.
     * <p>
     * Subscriptions that are inactive are always updated against ABN AMRO so that we have the latest data.
     */
    private AbnAmroUserLoginResponse createAbnAmroUserLoginResponse(UserLoginResponse loginResponse,
            AuthenticationRequest authenticationRequest, Optional<Date> lastLogin) {

        User user = loginResponse.getContext().getUser();

        AbnAmroUserLoginResponse abnLoginResponse = new AbnAmroUserLoginResponse(loginResponse);

        AbnAmroSubscription subscription = abnAmroSubscriptionRepository.findOneByUserId(user.getId());

        if (subscription == null) {
            subscription = createSubscription(authenticationRequest, user);
        } else if (!subscription.isActivated()) {
            subscription = updateSubscription(subscription, authenticationRequest);
        }

        abnLoginResponse.setSubscription(subscription);

        AbnAmroAccountUpdatesConfiguration updateConfig = configuration.getInternetBankingConfiguration()
                .getAccountUpdates();

        // Only check for new accounts if the user is eligible for a new account check
        if (AbnAmroAccountsUpdateChecker.isEligibleForAccountUpdates(updateConfig, user, subscription, lastLogin)) {
            abnLoginResponse.setNotSubscribedContracts(getNewContractNumbers(user, authenticationRequest));
        }

        return abnLoginResponse;
    }

    private List<Long> getNewContractNumbers(User user, AuthenticatedRequest authenticatedRequest) {

        List<Account> availableAccounts = getAvailableAbnAmroAccounts(user, authenticatedRequest);

        List<Account> existingAccounts = accountRepository.findByUserId(user.getId());

        if (AbnAmroUtils.shouldUseNewIcsAccountFormat(user)) {
            availableAccounts.forEach(a -> a.setBankId(AbnAmroUtils.getIcsShortBankId(a)));
            existingAccounts.forEach(a -> a.setBankId(AbnAmroUtils.getIcsShortBankId(a)));
        }

        List<Account> difference = AbnAmroUtils.getAccountDifference(availableAccounts, existingAccounts);

        try {
            // Temporary code to log the accounts that are available at Tink but unavailable at ABN AMRO. This is most
            // likely related to the "CJ-Split" issue where the user doesn't have access to the contract. This
            // log is here to be able to see how big the problem is, should be removed once CJ split is implemented by
            // ABN AMRO.
            List<Account> removedAccounts = AbnAmroUtils.getAccountDifference(existingAccounts, availableAccounts);
            for (Account account : removedAccounts) {
                Map<String, String> payload = account.getPayloadAsMap();
                payload.put(AbnAmroUtils.InternalAccountPayloadKeys.LOCKED, "locked");
                account.setPayload(SerializationUtils.serializeToString(payload));
                log.info(user.getId(), String.format("Account unavailable (Account = '%s')", account.getBankId()));
            }
            accountRepository.save(removedAccounts);
        } catch (Exception e) {
            log.error(user.getId(), "Error when calculating account difference", e);
        }


        return difference.stream().map(account -> Long.valueOf(account.getBankId())).collect(Collectors.toList());
    }

    private AbnAmroSubscription createSubscription(AuthenticationRequest request, User user) {

        AbnAmroSubscription subscription = new AbnAmroSubscription();
        subscription.setUserId(user.getId());

        return updateSubscription(subscription, request);
    }

    /**
     * Create or updates a subscription by subscribing the user against ABN AMRO.
     * <p/>
     * Subscription will be marked as active if the user already have a active/signed subscription. This could happen
     * if the user has activated/signed the subscription at ABN AMRO but we haven't received the call to
     * `abnamro/activate`.
     */
    private AbnAmroSubscription updateSubscription(AbnAmroSubscription subscription, AuthenticationRequest request) {

        try {
            // Initialize a subscription request to ABN to get the subscription id
            IBSubscriptionClient client = createIBSubscriptionClient();

            Long subscriptionId = client.subscribe(request);
            subscription.setSubscriptionId(subscriptionId);

            tracker.trackEvent(
                    TrackableEvent.event(subscription.getUserId(), "pfm.subscription-created", Maps.newHashMap()));

        } catch (AlreadySubscribedCustomerException e) {
            // This try-catch flow is needed since it is the only way to check if a user has signed a subscription.
            // It can be changed when ABN AMRO has added the new get-operation.
            subscription.setActivationDate(new Date());
        } catch (NonRetailCustomerException e) {
            httpResponseHelper.error(Status.FORBIDDEN, "Non retail customer tried to create subscription.");
        } catch (UnderAge16CustomerException e) {
            httpResponseHelper.error(Status.FORBIDDEN, "Customer under age of 16 tried to create subscription.");
        } catch (SubscriptionException e) {
            httpResponseHelper.error(Status.INTERNAL_SERVER_ERROR, "Could not subscribe customer.");
        } catch (Exception e) {
            httpResponseHelper.error(Status.INTERNAL_SERVER_ERROR, "Could not call subscription service.", e);
        }

        abnAmroSubscriptionRepository.save(subscription);

        return subscription;
    }

    private IBSubscriptionClient createIBSubscriptionClient() {

        try {
            return new IBSubscriptionClient(configuration, metricRegistry);
        } catch (Exception e) {
            httpResponseHelper.error(Status.INTERNAL_SERVER_ERROR, "Unable to set up secure client.", e);
        }

        return null;
    }

    @Override
    public Credentials credentials(AuthenticatedUser authenticatedUser, AccountSubscriptionRequest request) {

        User user = authenticatedUser.getUser();

        validate(request);

        validateUser(user, request);

        validateSubscription(abnAmroSubscriptionRepository.findOneByUserId(user.getId()));

        Credentials credentials = getOrCreateAbnAmroCredential(user, request);

        // Don't continue if we already have a subscription in progress. We can't handle multiple subscriptions at once
        // in the connector.
        if (isUpdating(credentials)) {
            log.warn(credentials, "Subscription is already in progress.");
            return credentials;
        }

        // Set the credentials to `AUTHENTICATING` until the subscription call to ABN AMRO is completed.
        credentials.setStatus(CredentialsStatus.AUTHENTICATING);
        credentials.setStatusPayload(null);

        // Update the credentials.
        updateCredentials(credentials);

        // Fetched the saved credentials anew (to get the updated information).
        credentials = credentialsRepository.findOne(credentials.getId());

        // Get all external accounts
        List<Account> accounts = getExternalAccounts(user, credentials, request);

        // Get existing accounts
        List<Account> existingAccounts = accountRepository.findByUserId(user.getId());

        // Compare the external accounts against the accounts that the user already has. We can only subscribe
        // accounts that we haven't subscribed before.
        List<Account> difference = AbnAmroUtils.getAccountDifference(accounts, existingAccounts);

        // No accounts found. We are not waiting on transactions from the connector. Mark the status as updated.
        if (difference.isEmpty()) {

            credentials.setStatus(CredentialsStatus.UPDATED);
            credentials.setStatusPayload(null);

            updateCredentials(credentials);
        } else {
            createAndSubscribeAccounts(user, authenticatedUser, credentials, difference, request);
        }

        // This should be removed since we don't use this in the new clients
        return credentials;
    }

    /**
     * Creates the accounts in the Tink backend and subscribe the accounts towards ABN AMRO. Transactions are sent
     * to the connector for all accounts except credit cards.
     */
    private void createAndSubscribeAccounts(User user, AuthenticatedUser authenticatedUser,
            Credentials credentials, List<Account> accounts, AccountSubscriptionRequest request) {

        // 1. Filter out the non credit card accounts and create them in at Tink
        List<Account> nonCreditCardAccounts = getNonCreditCardAccounts(accounts);

        abnAmroAccountController.updateAccounts(credentials, nonCreditCardAccounts);

        // 2. Create "buffered accounts" for these accounts to indicate that we are expecting transactions on them in
        // the connector
        List<AbnAmroBufferedAccount> bufferedAccounts = createBufferedAccounts(credentials, nonCreditCardAccounts);

        // 3. Execute the subscription call to ABN AMRO to subscribe all accounts even credit cards
        Optional<SubscriptionResult> subscriptionResult = subscribe(user, credentials, request, accounts);

        // Will not happen since subscribe call is throwing SERVICE_UNAVAILABLE
        if (!subscriptionResult.isPresent()) {
            log.error(credentials, "Subscription failed.");
            return;
        }

        // 4. Mark all rejected accounts as rejected and set the buffered account for them as completed since no
        // transactions will be sent to them.
        updateRejectedAccounts(credentials, bufferedAccounts,
                getNonCreditCardAccounts(subscriptionResult.get().getRejectedAccounts()));

        // 5. Filter out the credit card accounts and create or update credentials and accounts for them. This call
        // needs to be executed after the subscription call since we aren't allowed to retrieve transactions if the
        // accounts haven't been subscribed.
        List<Account> creditCardAccounts = getCreditCardAccounts(
                subscriptionResult.get().getSubscribedAccounts());
        if (AbnAmroUtils.shouldUseNewIcsAccountFormat(user)) {
            creditCardAccounts.forEach(acc -> acc.setBankId(AbnAmroUtils.creditCardIdToAccountId(acc.getBankId())));
        }

        abnAmroCreditCardController.updateCredentials(authenticatedUser, creditCardAccounts);
    }

    private void updateRejectedAccounts(Credentials credentials, List<AbnAmroBufferedAccount> bufferedAccounts,
            List<Account> rejectedAccounts) {

        List<AbnAmroBufferedAccount> rejectedBufferedAccounts = filterBufferedAccountsByAccountNumber(bufferedAccounts,
                rejectedAccounts);

        for (AbnAmroBufferedAccount rejected : rejectedBufferedAccounts) {
            rejected.setComplete(true);
        }

        // Update the buffered accounts that was rejected as completed since we won't receive data for them
        abnAmroBufferedAccountRepository.save(rejectedBufferedAccounts);

        // Update the rejected accounts
        abnAmroAccountController.updateAccounts(credentials, rejectedAccounts);
    }

    private List<AbnAmroBufferedAccount> createBufferedAccounts(final Credentials credentials, List<Account> accounts) {

        // Mark that we are waiting on data on the subscribed accounts by creating the buffered accounts
        List<AbnAmroBufferedAccount> bufferedAccounts = accounts.stream()
                .map(account -> AbnAmroBufferedAccount.create(credentials.getId(), account.getBankId()))
                .collect(Collectors.toList());

        return abnAmroBufferedAccountRepository.save(bufferedAccounts);
    }

    private static List<AbnAmroBufferedAccount> filterBufferedAccountsByAccountNumber(
            List<AbnAmroBufferedAccount> bufferedAccounts, List<Account> accounts) {

        final Set<Long> accountNumbers = accounts.stream().map(a -> Long.valueOf(a.getBankId()))
                .collect(Collectors.toSet());

        return bufferedAccounts.stream()
                .filter(abnAmroBufferedAccount -> accountNumbers.contains(abnAmroBufferedAccount.getAccountNumber()))
                .collect(Collectors.toList());
    }

    private void validateSubscription(AbnAmroSubscription subscription) {
        if (subscription == null) {
            httpResponseHelper.error(Status.FORBIDDEN, "Subscription does not exist.");
        }

        if (!subscription.isActivated()) {
            httpResponseHelper.error(Status.FORBIDDEN, "Subscription is not activated.");
        }
    }

    private Credentials getOrCreateAbnAmroCredential(User user, AccountSubscriptionRequest request) {
        List<Credentials> abnAmroCredentials = credentialsRepository.findAllByUserIdAndProviderName(user.getId(),
                AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);

        if (abnAmroCredentials.size() > 1) {
            httpResponseHelper.error(Status.PRECONDITION_FAILED, "Invalid credentials count.");
        }

        Credentials credentials = Iterables.getFirst(abnAmroCredentials, null);

        // No ABN AMRO credentials currently exist. Create!
        return credentials == null ? createCredentials(user, request) : credentials;
    }

    private List<Account> getExternalAccounts(User user, Credentials credentials, AccountSubscriptionRequest request) {

        final Catalog catalog = Catalog.getCatalog(user.getLocale());

        List<Account> accounts = getAvailableAbnAmroAccounts(user, request);

        // Bank ids for the accounts the user wants to subscribe to.
        Set<String> subscribeBankIds = request.getAccounts().stream().map(AbnAmroUtils::getBankId)
                .collect(Collectors.toSet());

        // Enable accounts that are subscribed to, and exclude accounts that are not.
        for (Account account : accounts) {
            account.setExcluded(!subscribeBankIds.contains(account.getBankId()));
        }

        // We consider it an error if we can't retrieve any accounts from ABN AMRO
        if (accounts.isEmpty()) {
            updateCredentialsStatus(credentials, CredentialsStatus.AUTHENTICATION_ERROR,
                    catalog.getString("Unable to fetch accounts."));
            httpResponseHelper.error(Status.SERVICE_UNAVAILABLE, "No accounts to subscribe to.");
        }

        return accounts;
    }

    private boolean isUpdating(Credentials credentials) {
        return Objects.equals(credentials.getStatus(), CredentialsStatus.UPDATING) || Objects
                .equals(credentials.getStatus(), CredentialsStatus.AUTHENTICATING);
    }

    private void validateUser(User user, AccountSubscriptionRequest request) {
        validateUser(user, request.getBcNumber());
    }

    private void validateUser(User user, SubscriptionActivationRequest request) {
        validateUser(user, request.getBcNumber());
    }

    private void validateUser(User user, String bcNumber) {
        if (!equal(AbnAmroLegacyUserUtils.getUsername(bcNumber), user.getUsername())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Invalid bc number.");
        }
    }

    @Override
    public String ping() {
        return "pong";
    }

    /**
     * Activate an account/subscription. The is done checking against ABN AMRO if the user has singed the T&C
     * and not and then updating the activation date on the subscription.
     * <p/>
     * There is special logic for pilot users. They already have credentials and accounts so we are re-subscribing
     * the accounts towards ABN AMRO again. (Need to do this since they can't migrated the subscriptions).
     * <p/>
     * This method is only called for non-pilot environment.
     */
    @Override
    public void activate(@Authenticated User user, SubscriptionActivationRequest request) {

        validate(request);

        validateUser(user, request);

        log.info(user.getId(), "Activating subscription.");

        AbnAmroSubscription subscription = abnAmroSubscriptionRepository.findOneByUserId(user.getId());

        if (subscription == null) {
            httpResponseHelper.error(Status.FORBIDDEN, "Subscription not found.");
        }

        // No need to do anything if the subscription already is active
        if (subscription.isActivated()) {
            log.info(user.getId(), "Subscription already active.");
            return;
        }

        IBSubscriptionClient client = createIBSubscriptionClient();

        if (client.isSubscriptionActivated(request)) {
            subscription.setActivationDate(new Date());
            tracker.trackEvent(
                    TrackableEvent.event(subscription.getUserId(), "pfm.subscription-activated", Maps.newHashMap()));
        } else {
            httpResponseHelper.error(Status.FORBIDDEN, "Subscription not activated at ABN AMRO.");
        }

        if (user.getFlags().contains(FeatureFlags.ABN_AMRO_PILOT_CUSTOMER)) {
            log.info(user.getId(), "Re-subscribing accounts for pilot customer.");
            resubscribeAccounts(user, request);
        }

        abnAmroSubscriptionRepository.save(subscription);
    }

    private void resubscribeAccounts(User user, SubscriptionActivationRequest request) {

        if (user.getFlags().contains(FeatureFlags.TINK_TEST_ACCOUNT)) {
            log.info(user.getId(), "Not re-subscribing accounts for test accounts.");
            return;
        }

        List<Credentials> credentialsList = credentialsRepository.findAllByUserIdAndProviderName(user.getId(),
                AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);

        if (credentialsList.size() != 1) {
            httpResponseHelper.error(Status.PRECONDITION_FAILED, "Invalid credentials count.");
        }

        Credentials credentials = credentialsList.get(0);

        List<Account> accounts = accountRepository.findByUserIdAndCredentialsId(user.getId(), credentials.getId());

        AccountSubscriptionRequest subscriptionRequest = new AccountSubscriptionRequest();

        subscriptionRequest.setSessionToken(request.getSessionToken());
        subscriptionRequest.setBcNumber(request.getBcNumber());
        subscriptionRequest.setAccounts(getAccountNumbers(accounts));

        subscribe(user, credentials, subscriptionRequest, accounts);
    }

    @Deprecated
    @Override
    public void validate(CustomerValidationRequest request) {

        // ABN AMRO is down for maintenance. Since account subscription will be unavailable, disable registration.
        if (configuration.isDownForMaintenance()) {
            httpResponseHelper.error(Status.SERVICE_UNAVAILABLE,
                    "Registration is currently disabled due to maintenance.");
        }
    }

    private Credentials createCredentials(User user, AccountSubscriptionRequest request) {
        return createCredentials(user, request.getBcNumber());
    }

    private Credentials createCredentials(User user, AuthenticationRequest request) {
        return createCredentials(user, request.getBcNumber());
    }

    private Credentials createCredentials(User user, String bcNumber) {

        Credentials credentials = new Credentials();

        credentials.setField(AbnAmroUtils.BC_NUMBER_FIELD_NAME, bcNumber);
        credentials.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUserId(user.getId());

        return credentialsRepository.save(credentials);
    }

    private User createUser(AuthenticationRequest request) {

        Market market = marketServiceController.getMarket(Market.Code.NL.name());

        String locale = request.getLocale();

        if (locale != null && !AbnAmroUtils.isValidLocale(locale)) {
            log.warn(String.format("Unsupported locale. Using market default. (Customer = '%s', Locale = '%s')",
                    request.getBcNumber(), locale));
            locale = null;
        }

        User user = new User();
        user.setProfile(UserProfile.createDefault(market, locale));
        user.setUsername(AbnAmroLegacyUserUtils.getUsername(request));
        user.setFlags(Lists.<String>newArrayList());

        return user;
    }

    private static List<Long> getAccountNumbers(List<Account> accounts) {
        return accounts.stream().map(input -> Long.valueOf(input.getBankId())).collect(Collectors.toList());
    }

    private List<Account> getAvailableAbnAmroAccounts(User user, AuthenticatedRequest request) {
        try {
            IBClient ibClient = new IBClient(configuration, metricRegistry);

            return ibClient.getAccounts(request, new Locale(user.getLocale()).getLanguage(),
                    AbnAmroUtils.shouldUseNewIcsAccountFormat(user));
        } catch (Exception e) {
            log.error("Unable to set up secure client.", e);
            return Lists.newArrayList();
        }
    }

    private UserLoginResponse login(User user) {
        UserService userService = serviceContext.getServiceFactory().getUserService();
        return userService.login(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), null, user);
    }

    private UserLoginResponse register(AuthenticationRequest request) {
        if (AbnAmroLegacyUserUtils.isTestUser(request.getBcNumber())) {
            return registerTestUser(request);
        } else {
            return registerNormalUser(request);
        }
    }

    private UserLoginResponse registerNormalUser(AuthenticationRequest request) {
        User user = createUser(request);
        return serviceContext.getServiceFactory().getUserService().register(user);
    }

    private UserLoginResponse registerTestUser(AuthenticationRequest request) {
        UserService userService = serviceContext.getServiceFactory().getUserService();

        User user = createUser(request);

        user.getFlags().add(FeatureFlags.TINK_TEST_ACCOUNT);

        UserLoginResponse response = userService.register(user);

        user = response.getContext().getUser();

        // Set period break to salary the 25th.
        user.getProfile().setPeriodAdjustedDay(25);
        user.getProfile().setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        userService.updateProfile(user, user.getProfile());

        // Create a credential and import test transactions
        createTestAccountsAndTransactions(createCredentials(user, request));

        // Update with a fresh context to make the app login user directly
        response.setContext(userService.getContext(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user)));
        return response;
    }

    /**
     * Subscribe accounts against ABN AMRO.
     * <p>
     * ABN AMRO credentials will be set to authentication error if the subscription call failed.
     * Any rejected/failed contracts will be updated and marked as failed with the reason why they failed.
     */
    private Optional<SubscriptionResult> subscribe(User user, Credentials credentials, AuthenticatedRequest request,
            List<Account> accounts) {

        final Catalog catalog = Catalog.getCatalog(user.getLocale());

        // Update the credential status to indicate that we are waiting on data.
        updateCredentialsStatus(credentials, CredentialsStatus.UPDATING, null);

        try {
            return Optional.of(subscribeAccounts(accounts, request));
        } catch (Exception e) {

            log.error(credentials, "Could not call subscription service.", e);

            updateCredentialsStatus(credentials, CredentialsStatus.AUTHENTICATION_ERROR,
                    catalog.getString("Unable to subscribe to accounts."));
            httpResponseHelper.error(Status.SERVICE_UNAVAILABLE, "Could not subscribe accounts.");
        }

        return Optional.empty();
    }

    /**
     * Subscribe accounts against ABN AMRO.
     * The rejected accounts are updated with reason and date when they where rejected.
     */
    private SubscriptionResult subscribeAccounts(List<Account> accounts, AuthenticatedRequest request)
            throws Exception {

        IBSubscriptionClient client = new IBSubscriptionClient(configuration, metricRegistry);

        SubscriptionAccountsRequest subscriptionRequest = new SubscriptionAccountsRequest();
        subscriptionRequest.setBcNumber(request.getBcNumber());
        subscriptionRequest.setContracts(getAccountNumbers(accounts));

        List<RejectedContractEntity> rejectedContracts = client
                .subscribeAccountsWithSession(request.getSessionToken(), subscriptionRequest);

        return new SubscriptionResultBuilder().withAccounts(accounts).withRejectedContracts(rejectedContracts).build();
    }

    private void createTestAccountsAndTransactions(Credentials credentials) {

        Preconditions.checkNotNull(credentials, "Credentials should not be null");

        // Verify that credential is empty before adding accounts and transactions
        List<Account> existingAccounts = accountRepository.findByCredentialsId(credentials.getId());

        Preconditions.checkState(existingAccounts.size() == 0, "Credential should be empty");

        List<Account> updatedAccounts = Lists.newArrayList();

        for (Account account : AbnAmroTestDataUtils.getTestAccounts(credentials)) {
            UpdateAccountRequest updateAccountRequest = new UpdateAccountRequest();
            updateAccountRequest.setUser(credentials.getUserId());
            updateAccountRequest.setCredentialsId(credentials.getId());
            updateAccountRequest.setAccount(account);

            Account updatedAccount = systemServiceFactory.getUpdateService().updateAccount(updateAccountRequest);

            updatedAccounts.add(updatedAccount);
        }

        UpdateTransactionsRequest transactionsRequest = new UpdateTransactionsRequest();
        transactionsRequest.setUser(credentials.getUserId());
        transactionsRequest.setUserTriggered(true);
        transactionsRequest.setCredentials(credentials.getId());
        transactionsRequest.setTransactions(
                CoreTransactionMapper.toSystemTransaction(AbnAmroTestDataUtils.getTestTransactions(updatedAccounts)));

        systemServiceFactory.getProcessService().updateTransactionsSynchronously(transactionsRequest);
    }

    private void updateCredentials(Credentials credentials) {

        UpdateCredentialsStatusRequest request = new UpdateCredentialsStatusRequest();

        request.setCredentials(credentials);
        request.setUpdateContextTimestamp(true);
        request.setUserId(credentials.getUserId());

        systemServiceFactory.getUpdateService().updateCredentials(request);
    }

    private void updateCredentialsStatus(Credentials credentials, CredentialsStatus status, String statusPayload) {
        credentials.setStatus(status);
        credentials.setStatusPayload(statusPayload);
        updateCredentials(credentials);
    }

    private void validate(AuthenticatedRequest request) {
        if (Strings.isNullOrEmpty(request.getBcNumber())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Missing bc number.");
        }

        if (Strings.isNullOrEmpty(request.getSessionToken())) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Missing session token.");
        }
    }

    private static List<Account> getNonCreditCardAccounts(List<Account> accounts) {
        return accounts.stream().filter(a -> !Objects.equals(AccountTypes.CREDIT_CARD, a.getType()))
                .collect(Collectors.toList());
    }

    private static List<Account> getCreditCardAccounts(List<Account> accounts) {
        return accounts.stream().filter(a -> Objects.equals(AccountTypes.CREDIT_CARD, a.getType()))
                .collect(Collectors.toList());
    }
}
