package se.tink.backend.main.resources;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.api.OAuth2Service;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.bankid.signicat.SignicatBankIdAuthenticator;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.common.repository.cassandra.OAuth2ClientEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizationRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizeRequestRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserOAuth2ClientRoleRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.utils.CommonStringUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.OAuth2ClientEvent;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.core.UserOAuth2ClientRole;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.oauth2.OAuth2AuthenticationTokenResponse;
import se.tink.backend.core.oauth2.OAuth2Authorization;
import se.tink.backend.core.oauth2.OAuth2AuthorizationDescription;
import se.tink.backend.core.oauth2.OAuth2AuthorizeRequest;
import se.tink.backend.core.oauth2.OAuth2AuthorizeResponse;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2ClientProductMetaData;
import se.tink.backend.core.oauth2.OAuth2ClientScopes;
import se.tink.backend.core.oauth2.OAuth2Converters;
import se.tink.backend.core.oauth2.OAuth2Utils;
import se.tink.backend.exception.jersey.JerseyRequestError;
import se.tink.backend.exception.jersey.JerseyRequestException;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.controllers.EmailAndPasswordAuthenticationServiceController;
import se.tink.backend.main.utils.BestGuessSwedishSSNHelper;
import se.tink.backend.rpc.ForgotPasswordCommand;
import se.tink.backend.rpc.OAuth2ClientListResponse;
import se.tink.backend.rpc.oauth.OAuthPartnerRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.CounterCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.oauth.GrantType;
import se.tink.libraries.oauth.RedirectUriChecker;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.oauth.OAuthGrpcClient;
import se.tink.oauth.grpc.Client;

@Path("/api/v1/oauth")
public class OAuth2ServiceResource implements OAuth2Service {
    private static final LogUtils log = new LogUtils(OAuth2ServiceResource.class);
    private static final Splitter SCOPE_COMPONENT_SPLITTER = Splitter.on(":").trimResults().omitEmptyStrings();
    private static final Joiner SCOPE_COMPONENT_JOINER = Joiner.on(":");

    private final LoadingCache<MetricId.MetricLabels, Counter> authorizeMeterCache;
    private final LoadingCache<MetricId.MetricLabels, Counter> refreshMeterCache;
    private final ServiceContext serviceContext;
    private final UserDeviceController userDeviceController;
    private final CredentialsRepository credentialsRepository;
    private final OAuth2AuthorizationRepository authorizationRepository;
    private final OAuth2AuthorizeRequestRepository authorizeRequestRepository;
    private final OAuth2ClientRepository clientRepository;
    private final OAuth2ClientEventRepository oauth2ClientEventRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserOAuth2ClientRoleRepository userOAuth2ClientRoleRepository;
    private OAuthGrpcClient oAuthGrpcClient;
    private MailSender mailSender;
    private EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController;
    private final HttpResponseHelper httpResponseHelper;

    @Context
    private HttpHeaders headers;

    public OAuth2ServiceResource(ServiceContext context, MetricRegistry metricRegistry,
            OAuthGrpcClient oAuthGrpcClient, MailSender mailSender,
            EmailAndPasswordAuthenticationServiceController emailAndPasswordAuthenticationServiceController) {
        this.serviceContext = context;

        this.credentialsRepository = context.getRepository(CredentialsRepository.class);
        this.clientRepository = context.getRepository(OAuth2ClientRepository.class);
        this.authorizeRequestRepository = context.getRepository(OAuth2AuthorizeRequestRepository.class);
        this.authorizationRepository = context.getRepository(OAuth2AuthorizationRepository.class);
        this.oauth2ClientEventRepository = context.getRepository(OAuth2ClientEventRepository.class);
        this.userDeviceRepository = context.getRepository(UserDeviceRepository.class);
        this.userOAuth2ClientRoleRepository = context.getRepository(UserOAuth2ClientRoleRepository.class);
        this.oAuthGrpcClient = oAuthGrpcClient;
        this.mailSender = mailSender;
        this.emailAndPasswordAuthenticationServiceController = emailAndPasswordAuthenticationServiceController;

        this.httpResponseHelper = new HttpResponseHelper(log);
        this.userDeviceController = new UserDeviceController(userDeviceRepository);

        this.authorizeMeterCache = CacheBuilder.newBuilder().build(new CounterCacheLoader(
                metricRegistry,
                MetricId.newId("exchange_authorization_code"))
        );

        this.refreshMeterCache = CacheBuilder.newBuilder().build(new CounterCacheLoader(
                metricRegistry,
                MetricId.newId("exchange_refresh_token"))
        );
    }

    @Override
    public OAuth2AuthorizeResponse authorize(User user, OAuth2AuthorizeRequest request) {
        OAuth2Client client = null;

        // TODO: nina.olofsson@tink.se: testing OAuth gRPC here. Will switch completely to gRPC.

        if (user.getFlags().contains(FeatureFlags.TMP_TEST_OAUTH_GRPC) && oAuthGrpcClient != null) {
            log.info(user.getId(), "Fetching data from OAuth gRPC client");
            Optional<Client> grpcClient = oAuthGrpcClient.getClient(request.getClientId());
            if (grpcClient.isPresent()) {
                client = OAuth2Converters.toClient(grpcClient.get());
            }
        } else {
            client = clientRepository.findOne(request.getClientId());
        }

        if (client == null) {
            httpResponseHelper.error(Response.Status.UNAUTHORIZED, "Client not found");
        }

        if (!client.getRedirectUris().contains(request.getRedirectUri())) {
            httpResponseHelper.error(Response.Status.UNAUTHORIZED, "Redirect URI is not valid");
        }

        if (!client.getOAuth2Scope().isRequestedScopeValid(request.getScope())) {
            httpResponseHelper.error(Response.Status.UNAUTHORIZED,
                    "Requested scope is not valid: " + request.getScope());
        }

        OAuth2AuthorizeRequest existingAuthRequest = authorizeRequestRepository.findOneByUserIdAndClientId(
                user.getId(), client.getId());

        if (existingAuthRequest != null) {
            // Have existing auth request for same user and same OAuth2 Client.
            // The sleep here is to help our partners to increase the chance of get the successful request before the
            // failed one here. This can happen if the end user duplicates a tab in the middle of the signing process.
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
            httpResponseHelper.error(Response.Status.CONFLICT, "Multiple Authorization Requests.");
        }

        request.setUserId(user.getId());

        authorizeRequestRepository.save(request);
        oauth2ClientEventRepository.save(
                OAuth2ClientEvent.createUserAuthorizedEvent(client.getId(), user.getId()));

        OAuth2AuthorizeResponse response = new OAuth2AuthorizeResponse();
        response.setCode(request.getCode());

        return response;
    }

    @Override
    public OAuth2AuthorizeResponse autoAuthorize(User user, OAuth2AuthorizeRequest request) {
        String clientId = request.getClientId();

        OAuth2Client client = clientRepository.findOne(clientId);

        if (!client.hasAutoAuthorize()) {
            httpResponseHelper.error(Response.Status.UNAUTHORIZED, "Auto-authorize not available");
        }

        return authorize(user, request);
    }

    @Override
    public OAuth2AuthenticationTokenResponse token(String clientId, String clientSecret, String grantType, String code,
            String refreshToken) throws JerseyRequestException {

        if (Strings.isNullOrEmpty(clientId)) {
            throw JerseyRequestError.Oauth.NO_CLIENT_ID_IN_REQUEST.exception();
        }

        OAuth2Client client = clientRepository.findOne(clientId);

        if (client == null) {
            throw JerseyRequestError.Oauth.OAUTH_CLIENT_NOT_FOUND.exception();
        }

        if (client.hasAutoAuthorize()) {
            // We currently only allow grant_type authorization_code for auto-authorize.

            // FIXME: the token endpoint doesn't know if this current request is auto-auth or normal auth
            // FIXME: now we won't allow grant_type implicit if OAuth2 client has auto-auth available.

            if (!Objects.equals(grantType, GrantType.AUTHORIZATION_CODE.getName())) {
                throw JerseyRequestError.Oauth.AUTO_AUTH_GRANT_TYPE_NOT_ALLOWED.exception();
            }
        }

        if (!Objects.equals(client.getSecret(), clientSecret)) {
            throw JerseyRequestError.Oauth.INVALID_SECRET.exception();
        }

        if (Objects.equals(grantType, GrantType.AUTHORIZATION_CODE.getName())) {
            OAuth2AuthorizeRequest request = authorizeRequestRepository.findOne(code);

            if (request == null) {
                throw JerseyRequestError.Oauth.CODE_NOT_FOUND.exception();
            }

            if (!Objects.equals(request.getClientId(), clientId)) {
                throw JerseyRequestError.Oauth.CLIENT_ID_DOES_NOT_MATCH_CODE.exception();
            }

            if (!client.getOAuth2Scope().isRequestedScopeValid(request.getScope())) {
                throw JerseyRequestError.Oauth.INVALID_SCOPE.exception().withErrorDetails(request.getScope());
            }

            OAuth2Authorization authorization = new OAuth2Authorization();

            authorization.setScope(request.getScope());
            authorization.setUserId(request.getUserId());
            authorization.setClientId(client.getId());
            authorization.setCreated(new Date());
            authorization.refreshToken();

            authorizeRequestRepository.delete(request);
            authorizationRepository.save(authorization);

            authorizeMeterCache.getUnchecked(
                    new MetricId.MetricLabels().add("client", client.getName().toLowerCase())).inc();

            return new OAuth2AuthenticationTokenResponse(authorization);
        } else if (Objects.equals(grantType, GrantType.REFRESH_TOKEN.getName())) {
            OAuth2Authorization authorization = authorizationRepository.findByRefreshToken(refreshToken);

            if (authorization == null) {
                throw JerseyRequestError.Oauth.REFRESH_TOKEN_NOT_FOUND.exception();
            }

            if (!Objects.equals(authorization.getClientId(), clientId)) {
                throw JerseyRequestError.Oauth.CLIENT_ID_DOES_NOT_MATCH_REFRESH.exception();
            }

            authorization.refreshToken();

            authorizationRepository.save(authorization);

            refreshMeterCache.getUnchecked(
                    new MetricId.MetricLabels().add("client", client.getName().toLowerCase())).inc();

            return new OAuth2AuthenticationTokenResponse(authorization);
        }

        throw JerseyRequestError.Oauth.INVALID_GRANT_TYPE.exception().withErrorDetails(grantType);
    }

    @Override
    public OAuth2AuthorizationDescription describe(User user, OAuth2AuthorizeRequest request)
            throws JerseyRequestException {
        OAuth2Client client = clientRepository.findOne(request.getClientId());

        if (client == null) {
            throw JerseyRequestError.Oauth.OAUTH_CLIENT_NOT_FOUND.exception();
        }

        if (!client.getRedirectUris().contains(request.getRedirectUri())) {
            throw JerseyRequestError.Oauth.INVALID_REDIRECT_URI.exception();
        }

        OAuth2ClientScopes validScopesForClient = client.getOAuth2Scope();
        OAuth2ClientScopes scopeForDescription;

        if (!Strings.isNullOrEmpty(request.getScope())) {
            if (!validScopesForClient.isRequestedScopeValid(request.getScope())) {
                throw JerseyRequestError.Oauth.INVALID_SCOPE.exception();
            }
            scopeForDescription = new OAuth2ClientScopes(request.getScope());
        } else {
            scopeForDescription = client.getOAuth2Scope();
        }

        OAuth2AuthorizationDescription description = new OAuth2AuthorizationDescription();
        description.setClientName(client.getName());
        description.setClientUrl(client.getUrl());
        description.setClientIconUrl(client.getIconUrl());
        description.setScopesDescriptions(constructScopeDescription(user, scopeForDescription.getScopeSet()));
        description.setEmbeddedAllowed(client.getEmbeddedAllowed());

        return description;
    }

    @Override
    public OAuth2ClientProductMetaData getOAuth2ClientProductData(AuthenticatedUser user,
            OAuth2ClientRequest oauth2ClientRequest) {

        /* THIS IS AN METHOD FOR UNAUTHORIZED USERS -- NEED TO KEEP INFORMATION TO A MINIMUM */

        Optional<OAuth2Client> client = OAuth2Utils.getOAuth2Client(oauth2ClientRequest);

        if (!client.isPresent()) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Need LinkRequest and product data");
        }

        OAuth2ClientProductMetaData data = new OAuth2ClientProductMetaData();
        Optional<String> customCss = OAuth2Utils.getPayloadValue(client, OAuth2Client.PayloadKey.CUSTOM_CSS);

        data.setCustomCss(customCss.orElse(null));

        return data;
    }

    private static List<String> constructScopeDescription(User user, Set<String> scopes) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<String> readingSubjectDescriptions = Lists.newArrayList();
        List<String> writingSubjectDescriptions = Lists.newArrayList();
        List<String> otherDescriptions = Lists.newArrayList();

        for (String scope : scopes) {
            List<String> scopeComponents = Lists.newArrayList(SCOPE_COMPONENT_SPLITTER.split(scope));

            if (scopeComponents.size() == 1) {
                HttpResponseHelper.error(Response.Status.BAD_REQUEST);
            }

            boolean reading = "read".equals(scopeComponents.get(1));
            boolean writing = "write".equals(scopeComponents.get(1));

            if (reading || writing) {
                String subject = scopeComponents.get(0);

                String subjectDescription = null;

                if ("accounts".equals(subject)) {
                    subjectDescription = catalog.getString("accounts");
                } else if ("transactions".equals(subject)) {
                    if (scopeComponents.size() == 2) {
                        subjectDescription = catalog.getString("transactions");
                    } else {
                        String categoryCode = SCOPE_COMPONENT_JOINER.join(scopeComponents.subList(2,
                                scopeComponents.size() - 1));

                        if ("expenses-by-category".equals(categoryCode)) {
                            subjectDescription = catalog.getString("amount spent in each category");
                        } else if ("expenses-by-category/by-count".equals(categoryCode)) {
                            subjectDescription = catalog.getString("number of purchases in each category");
                        } else {
                            continue;
                        }
                    }
                } else if ("credentials".equals(subject)) {
                    subjectDescription = catalog.getString("bank connections");
                } else if ("user".equals(subject)) {
                    subjectDescription = catalog.getString("user profile");
                } else if ("follow".equals(subject)) {
                    subjectDescription = catalog.getString("budgets and savings goals");
                } else if ("documents".equals(subject)) {
                    subjectDescription = catalog.getString("documents");
                } else if ("payment_options".equals(subject)) {
                    subjectDescription = catalog.getString("payment options");
                } else if ("statistics".equals(subject)) {
                    if (scopeComponents.size() == 2) {
                        subjectDescription = catalog.getString("statistics");
                    } else {
                        String statisticsType = scopeComponents.get(2);

                        if ("expenses-by-category".equals(statisticsType)) {
                            subjectDescription = catalog.getString("amount spent in each category");
                        } else if ("expenses-by-category/by-count".equals(statisticsType)) {
                            subjectDescription = catalog.getString("number of purchases in each category");
                        } else {
                            continue;
                        }
                    }
                } else if ("investments".equals(subject)) {
                    subjectDescription = catalog.getString("investments");
                } else {
                    continue;
                }

                if (reading) {
                    readingSubjectDescriptions.add(subjectDescription);
                } else {
                    writingSubjectDescriptions.add(subjectDescription);
                }
            } else {
                if ("payments:execute".equals(scope)) {
                    // otherDescriptions.add(catalog.getString("Initiate payments"));
                } else {
                    System.out.println("Other: " + scope);

                }
            }
        }

        List<String> descriptions = Lists.newArrayList();

        if (!readingSubjectDescriptions.isEmpty()) {
            descriptions.add(Catalog.format(catalog.getString("View your {0}"),
                    CommonStringUtils.formatAndObjects(readingSubjectDescriptions, catalog)));
        }

        if (!writingSubjectDescriptions.isEmpty()) {
            descriptions.add(Catalog.format(catalog.getString("Update your {0}"),
                    CommonStringUtils.formatAndObjects(writingSubjectDescriptions, catalog)));
        }

        descriptions.addAll(otherDescriptions);

        return Lists.newArrayList(descriptions);
    }

    @Override
    public UserDevice authorizeUserDevice(final User user, String deviceId) {
        final UserDevice userDevice = userDeviceRepository.findOneByUserIdAndDeviceId(user.getId(), deviceId);
        final Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        if (userDevice == null) {
            httpResponseHelper.error(Response.Status.NOT_FOUND, "User Device not found");
        }

        if (userDevice.getStatus() != UserDeviceStatuses.UNAUTHORIZED) {
            return userDevice;
        }

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<Credentials> realCredentials = FluentIterable.from(credentials)
                .filter(Predicates.not(Predicates.IS_DEMO_CREDENTIALS))
                .toList();

        if (realCredentials.isEmpty()) {
            throw new IllegalStateException("Couldn't find any credentials");
        }

        // TODO: Launch BankID with autostarttoken instead of just guessing. Then check if any credential contains the SSN returned from BankID. Currently we just hope that the user has access to BankID for the guessed credential.
        Optional<String> personNumber = BestGuessSwedishSSNHelper
                .getBestGuessSwedishSSN(userDevice, realCredentials);

        // If no SSN is present we shouldn't end up here in the authorize device at all.
        if (!personNumber.isPresent()) {
            throw new IllegalStateException("Couldn't find any SSN for device authorization");
        }

        // Initiate authorization using BankID.
        userDevice.addPayload(UserDevice.PayloadKey.SWEDISH_SSN, personNumber.get());
        userDevice.setStatus(UserDeviceStatuses.AWAITING_BANKID_AUTHENTICATION);
        saveDevice(userDevice);

        SignicatBankIdAuthenticator authenticator = new SignicatBankIdAuthenticator(personNumber.get(),
                user.getId(), null, catalog, (status, statusPayload, authenticatedPersonNumber) -> {
            switch (status) {
            case AUTHENTICATED:
                userDevice.setStatus(UserDeviceStatuses.AUTHORIZED);
                break;
            case AUTHENTICATION_ERROR:
                userDevice.setStatus(UserDeviceStatuses.UNAUTHORIZED);
                break;
            case AWAITING_BANKID_AUTHENTICATION:
                userDevice.setStatus(UserDeviceStatuses.AWAITING_BANKID_AUTHENTICATION);
                break;
            default:
                log.error(user.getId(), "Unknown authentication status: " + status);
                userDevice.setStatus(UserDeviceStatuses.UNAUTHORIZED);
                break;
            }

            saveDevice(userDevice);
        });

        serviceContext.execute(authenticator);

        return userDevice;
    }

    private UserDevice saveDevice(UserDevice userDevice) {
        userDevice.setUpdated(new Date());
        userDeviceRepository.save(userDevice);

        return userDevice;
    }

    @Override
    public UserDevice getUserDevice(User user, String deviceId) {
        String userAgent = RequestHeaderUtils.getUserAgent(headers);

        UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);

        if (userDevice == null) {
            HttpResponseHelper.error(Response.Status.NOT_FOUND);
        }

        return userDevice;
    }

    @Override
    public OAuth2ClientListResponse getOauthClients(AuthenticatedUser authenticatedUser) {

        String userId = authenticatedUser.getUser().getId();

        List<UserOAuth2ClientRole> userRoles = userOAuth2ClientRoleRepository.findByUserId(userId);
        if (userRoles == null || userRoles.size() == 0) {
            log.warn(userId, "User does not have privileges.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        List<OAuth2Client> clients = Lists.newArrayList();

        for (UserOAuth2ClientRole role : userRoles) {
            if (Objects.equals(role.getRole(), UserOAuth2ClientRole.Role.ADMIN)) {
                OAuth2Client client = clientRepository.findOne(role.getClientId());
                if (client == null) {
                    log.warn(userId, "Misconfigured in database: client doesn't exist");
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                clients.add(client);
            }
        }

        OAuth2ClientListResponse response = new OAuth2ClientListResponse();
        response.setClients(clients);

        return response;
    }

    @Override
    public OAuth2Client updateOauthClient(AuthenticatedUser authenticatedUser, String id, OAuth2Client incomingClient)
            throws JerseyRequestException {
        String userId = authenticatedUser.getUser().getId();

        // Need to go via getOauthClient due to validation
        List<OAuth2Client> clients = getOauthClients(authenticatedUser).getClients();

        if (incomingClient == null) {
            log.warn(userId, "OAuth2Client was null");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        Optional<OAuth2Client> existingClient = clients.stream()
                .filter(Predicates.oauth2ClientById(id)::apply).findFirst();
        if (!existingClient.isPresent()) {
            log.warn(userId, "OAuth2Client was not available to user");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        for (String uri : incomingClient.getRedirectUris()) {
            boolean isValid = RedirectUriChecker.validate(uri);
            if (!isValid) {
                throw JerseyRequestError.Oauth.INVALID_REDIRECT_URI.exception().withErrorDetails(uri);
            }
        }

        OAuth2Client clientToSave = existingClient.get();
        clientToSave.setRedirectUris(incomingClient.getRedirectUris());
        clientToSave.setIconUrl(incomingClient.getIconUrl());

        return clientRepository.save(clientToSave);
    }

    /**
     * NOTE: this is a temporary endpoint. It will be removed when we have a proper portal for Growth partners where
     * they can sign up and get OAuth clients.
     */
    @Override
    public void registerNewOAuthClientForNewPartner(OAuthPartnerRequest request) throws JerseyRequestException {

        // Just a very simple, hard coded check to make sure the request comes from an app we have given the token to.
        if (!Objects.equals(request.getToken(), "98921af9bd9b47019f090237c72e6781")) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        if (!isValidEmailAddress(request.getEmail())) {
            throw JerseyRequestError.User.INVALID_EMAIL.exception();
        }

        String info = "<html><head></head><body>"
                + "<p>COMPANY_EMAIL: " + StringEscapeUtils.escapeHtml(request.getEmail()) + "</p>"
                + "<p>COMPANY_NAME: " + StringEscapeUtils.escapeHtml(request.getName()) + "</p>"
                + "<p>DESCRIPTION: " + StringEscapeUtils.escapeHtml(request.getDescription()) + "</p>";

        try {
            generateOAuthClient(request);
        } catch (Exception e) {
            if (!(e instanceof JerseyRequestException)) {
                info += "<p>STATUS: FAILED</p></body></html>";
                log.error("Could not register new partner", e);
                sendStatusEmail(info);
            }
            throw e;
        }
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the console service.
    private void sendStatusEmail(String email) {
        try {
            mailSender.sendMessage("api-support@tink.se", "New partner", "nina.olofsson@tink.se", "Growth", email);
        } catch (Exception e) {
            log.error("Could not send information about new partner to api-support", e);
        }
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the console service.
    private void generateOAuthClient(OAuthPartnerRequest request) throws JerseyRequestException {
        String clientId = UUIDUtils.generateUUID();
        String secret = UUIDUtils.generateUUID();

        String payload = "{\"PROVIDERS\": \"fi-aktia-codecard,fi-alandsbanken-codecard,fi-danskebank-codecard,fi-handelsbanken-codecard,fi-nordea-lightlogin,fi-omasp-codecard,fi-op-codecard,fi-poppankki-codecard,fi-saastopankki-codecard,fi-spankki-codecard,alandsbanken,americanexpress,avanza,avanza-bankid,chevroletmastercard-bankid,choicemastercard-bankid,collector-bankid,coop,csn,danskebank,danskebank-bankid,djurgardskortetmastercard-bankid,eurocard-bankid,finnairmastercard-bankid,forex,handelsbanken,handelsbanken-bankid,icabanken-bankid,ikanokort,jetmastercard-bankid,lansforsakringar,lansforsakringar-bankid,lysa,nknyckelnmastercard-bankid,nordea-bankid,nordnet,nordnet-bankid,norwegian-bankid,okq8bank,opelmastercard-bankid,preem-bankid,rikskortet,saabmastercard-bankid,saseurobonusamericanexpress,saseurobonusmastercard-bankid,savingsbank-bankid,savingsbank-bankid-youth,sbab-bankid,seatkortet,seb-bankid,sebwalletmastercard-bankid,shellmastercard,sjpriomastercard-bankid,skandiabanken-bankid,skandiabanken-ssn-bankid,skodakortet,sparbankensyd-bankid,statoilmastercard-bankid,supremecard-bankid,swedbank-bankid,swedbank-bankid-youth,volkswagenkortet,dk-albank-password,dk-almbrandbank-password,dk-andelskassen-password,dk-andelskassenfælleskassen-password,dk-andelskassenoikos-password,dk-banknordik-password,dk-basisbank-password,dk-bil-password,dk-borbjergsparekasse-password,dk-broagersparekasse-password,dk-coop-password,dk-danskebank-servicecode,dk-denjyskesparekasse-password,dk-djurslandsbank-password,dk-dragsholmsparekasse-password,dk-dronninglundsparekasse-password,dk-fanosparekasse-password,dk-fasterandelskasse-password,dk-folkesparekassen-password,dk-forbrugsforeningen-password,dk-frossparekasse-password,dk-frørupandelskasse-password,dk-frøslevmollerupsparekasse-password,dk-fynske-password,dk-grønlandsbanken-password,dk-handelsbanken-password,dk-hvidbjerg-password,dk-jakandelskasseostervra-password,dk-jutlanderbank-password,dk-jyskebank-codecard,dk-klimsparekasse-password,dk-københavnsandelskasse-password,dk-kreditbanken-password,dk-langasparekasse-password,dk-lansparbank-password,dk-lægernes-password,dk-lollands-password,dk-merkur-password,dk-middelfartsparekasse-password,dk-møns-password,dk-nordea-nemid,dk-nordfynsbank-password,dk-nordjyskebank-password,dk-nykredit-password,dk-ostjydskbank-password,dk-pensam-password,dk-pfa-password,dk-ringkjobinglandbobank-password,dk-riseflemloesesparekasse-password,dk-rondeparekasse-password,dk-salling-password,dk-saxoprivatbank-password,dk-sebwealth-password,dk-skjernbank-password,dk-søbyskaderhallingsparekasse-password,dk-sønderhahørstedsparekasse-password,dk-sparekassenballing-password,dk-sparekassenbredebro-password,dk-sparekassendenlillebikube-password,dk-sparekassendjursland-password,dk-sparekassenkronjylland-password,dk-sparekassennebelogomegn-password,dk-sparekassensjaelland-password,dk-sparekassenthy-password,dk-sparekassenvendsyssel-password,dk-sparnord-password,dk-stadilsparekasse-password,dk-swedbank-password,dk-sydbank-password,dk-totalbanken-password,dk-vestjysk-password,no-aasen-sparebank,no-afjord-sparebank,no-andebu-sparebank,no-arendal-og-omegns-sparekasse,no-askim-og-spydeberg-sparebank,no-aurland-sparebank,no-aurskog-sparebank,no-bank2,no-berg-sparebank,no-bien-sparebank,no-birkenes-sparebank,no-bjugn-sparebank,no-blaker-sparebank,no-bud-fraena-hustad-sparebank,no-cultura-bank,no-danskebank-password,no-dnb,no-drangedal-sparebank,no-easybank,no-eidsberg-sparebank,no-eika-kredittbank,no-etnedal-sparebank,no-evje-hornnes-sparebank,no-fornebubanken,no-gildeskal-sparebank,no-gjerstad-sparebank,no-grong-sparebank,no-grue-sparebank,no-haltdalen-sparebank,no-harstad-sparebank,no-hegra-sparebank,no-hemne-sparebank,no-hjartdal-gransherad-sparebank,no-hjelmeland-sparebank,no-honefoss-sparebank,no-høland-setskog-sparebank,no-indre-sogn-sparebank,no-jaeren-sparebank,no-jernbanepersonalets-bank-og-forsikring,no-klaebu-sparebank,no-kvinesdal-sparebank,no-larvikbanken,no-lillestrombanken,no-lofoten-sparebank,no-marker-sparebank,no-melhusbanken,no-nesset-sparebank,no-nordea-bankid,no-nordea-lightlogin,no-odal-sparebank,no-ofoten-sparebank,no-oppdalsbanken,no-orkla-sparebank,no-orland-sparebank,no-orskog-sparebank,no-personellservice-trondelag,no-rindal-sparebank,no-rorosbanken,no-sandnes-sparebank,no-selbu-sparebank,no-skagerrak-sparebank,no-skue-sparebank,no-soknedal-sparebank,no-sparebank1-bv,no-sparebank1-gudbrandsdal,no-sparebank1-hallingdal-valdres,no-sparebank1-lom-og-skjak,no-sparebank1-modum,no-sparebank1-nord-norge,no-sparebank1-nordvest,no-sparebank1-ostfold-akershus,no-sparebank1-ostlandet,no-sparebank1-ringerike-hadeland,no-sparebank1-smn,no-sparebank1-sore-sunnmore,no-sparebank1-sr-bank,no-sparebank1-telemark,no-sparebanken-din,no-sparebanken-narvik,no-sparebankenvest-activationcode,no-stadsbygd-sparebank,no-storebrand,no-strommen-sparebank,no-sunndal-sparebank,no-surnadal-sparebank,no-tinn-sparebank,no-tolga-os-sparebank,no-totens-sparebank,no-trogstad-sparebank,no-tysnes-sparebank,no-valle-sparebank,no-vang-sparebank,no-vekselbanken,no-vestre-slidre-sparebank,no-vik-sparebank,no-ya-bank\", \"REFRESHABLE_ITEMS\": \"ACCOUNTS,TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS,EINVOICES,TRANSFER_DESTINATIONS\", \"AUTO_AUTHORIZE\": \"false\", \"ALLOW_DEMO_CREDENTIALS\": \"false\", \"DOESNT_PRODUCE_TINK_USERS\": \"true\"}";
        String scope = "accounts:read,categories:read,contacts:read,credentials:read,documents:read,follow:read,statistics:read,transactions:read,user:read,suggestions:read,properties:read,providers:read,investments:read";
        String redirectUris = "[\"http://localhost:3000/callback\"]";

        log.info(String.format("Registering new partner:"
                        + "\nemail=%s\nname=%s\nurl=%s\npayload=%s\nscope=%s\nredirectUris=%s",
                request.getEmail(), request.getName(), null, payload, scope, redirectUris));

        User user = createAndGetUser(serviceContext, request.getEmail());

        sendSetPasswordEmail(request.getEmail());

        createClient(clientId, secret, serviceContext, redirectUris, payload, scope, request.getName());

        createRole(user.getId(), clientId, serviceContext);

        log.info(String.format("Created client with clientId=%s and userId=%s", clientId, user.getId()));
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the console service.
    private boolean isValidEmailAddress(String email) {
        boolean result = true;

        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (AddressException ex) {
            result = false;
        }

        return result;
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the console service.
    private void sendSetPasswordEmail(String email) {
        try {
            ForgotPasswordCommand command = new ForgotPasswordCommand(email,
                    RequestHeaderUtils.getRemoteIp(headers),
                    Optional.ofNullable(RequestHeaderUtils.getUserAgent(headers)));
            // TODO: change this to another email template when one exists in Mailchimp.
            emailAndPasswordAuthenticationServiceController.forgotPassword(command, MailTemplate.GROWTH_SIGNUP);
        } catch (InvalidEmailException e) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the oauth service.
    private void createRole(String userId, String clientId, ServiceContext serviceContext) {
        UserOAuth2ClientRoleRepository roleRepository = serviceContext
                .getRepository(UserOAuth2ClientRoleRepository.class);

        UserOAuth2ClientRole role = new UserOAuth2ClientRole();
        role.setClientId(clientId);
        role.setUserId(userId);
        role.setRole("ADMIN");

        roleRepository.save(role);
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the oauth service.
    private void createClient(String clientId, String secret, ServiceContext serviceContext, String redirectUris,
            String payload, String scope, String name) {
        OAuth2Client client = new OAuth2Client();
        client.setSecret(secret);
        client.setRedirectUrisSerialized(redirectUris);
        client.setPayloadSerialized(payload);
        client.setId(clientId);
        client.setUrl("");
        client.setScope(scope);
        client.setName(name);

        OAuth2ClientRepository clientRepository = serviceContext.getRepository(OAuth2ClientRepository.class);
        clientRepository.save(client);
    }

    // TODO: remove when no longer used by temporary endpoint. This logic should be in the console service.
    private User createAndGetUser(ServiceContext serviceContext, String email) throws JerseyRequestException {
        User user = new User();
        user.setUsername(email);
        UserProfile profile = new UserProfile();
        profile.setMarket("SE");
        profile.setLocale("en_US");
        profile.setCurrency("SEK");
        user.setProfile(profile);
        user.setCreated(new Date());

        HashingAlgorithm algorithm = serviceContext.getConfiguration().getAuthentication()
                .getUserPasswordHashAlgorithm();

        user.setHash(PasswordHash.create(UUIDUtils.generateUUID(), algorithm));

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        if (userRepository.findOneByUsername(email) != null) {
            throw JerseyRequestError.User.ALREADY_EXISTS.exception();
        }

        userRepository.save(user);

        return user;
    }
}
