package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.BARCLAYS_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.DANSKEBANK_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.GENERAL_STANDARD_ISS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.HSBC_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.MONZO_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.NATIONWIDE_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.NATWEST_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.RBS_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.RFC_2253_DN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.TINK_UK_OPEN_BANKING_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.UKOB_TAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingV31Constants.ULSTER_ORG_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants.ACCOUNT_PERMISSIONS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.TokenEndpointAuthMethod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.TrustedBeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.AccountsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.PartiesV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.PartyV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.TrustedBeneficiariesV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.JWTSignatureHeaders.HEADERS;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.JWTSignatureHeaders.PAYLOAD;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.JsonWebKeySet;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.TokenRequestForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class UkOpenBankingApiClient {

    private final TinkHttpClient httpClient;

    @Getter private final SoftwareStatementAssertion softwareStatement;

    @Getter private final String redirectUrl;

    @Getter private final ClientInfo providerConfiguration;

    @Getter private final JwtSigner signer;
    private final URL wellKnownURL;

    private WellKnownResponse cachedWellKnownResponse;
    private Map<String, PublicKey> cachedJwkPublicKeys;
    private UkOpenBankingAuthenticatedHttpFilter aisAuthFilter;
    private UkOpenBankingAuthenticatedHttpFilter pisAuthFilter;
    private ErrorEntity errorEntity;

    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;
    private static final List<String> POST_BASE64_GROUP =
            Arrays.asList(
                    DANSKEBANK_ORG_ID,
                    MONZO_ORG_ID,
                    NATIONWIDE_ORG_ID,
                    ULSTER_ORG_ID,
                    RBS_ORG_ID,
                    NATWEST_ORG_ID,
                    BARCLAYS_ORG_ID);

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig,
            UkOpenBankingPisConfig pisConfig) {
        this.httpClient = httpClient;
        this.softwareStatement = softwareStatement;
        this.redirectUrl = redirectUrl;
        this.providerConfiguration = providerConfiguration;
        this.signer = signer;
        this.wellKnownURL = aisConfig.getWellKnownURL();
        this.persistentStorage = persistentStorage;
        this.randomValueGenerator = randomValueGenerator;
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;

        addFilter(new ServiceUnavailableBankServiceErrorFilter());
        addFilter(new FinancialOrganisationIdFilter(aisConfig.getOrganisationId()));
    }

    void instantiateAisAuthFilter(OAuth2Token token) {
        log.debug("Instantiating the Ais Auth Filter.");
        aisAuthFilter = new UkOpenBankingAuthenticatedHttpFilter(token, randomValueGenerator);
    }

    void instantiatePisAuthFilter(OAuth2Token token) {
        log.debug("Instantiating the Pis Auth Filter.");
        pisAuthFilter = new UkOpenBankingAuthenticatedHttpFilter(token, randomValueGenerator);
    }

    void storeOpenIdError(ErrorEntity error) {
        errorEntity = error;
    }

    public Optional<ErrorEntity> getErrorEntity() {
        return Optional.ofNullable(errorEntity);
    }

    Optional<Map<String, PublicKey>> getJwkPublicKeys() {
        if (Objects.nonNull(cachedJwkPublicKeys)) {
            return Optional.of(cachedJwkPublicKeys);
        }

        String response =
                httpClient.request(getWellKnownConfiguration().getJwksUri()).get(String.class);

        JsonWebKeySet jsonWebKeySet =
                SerializationUtils.deserializeFromString(response, JsonWebKeySet.class);

        if (jsonWebKeySet == null) {
            return Optional.empty();
        }

        cachedJwkPublicKeys = jsonWebKeySet.getAllKeysMap();
        return Optional.ofNullable(cachedJwkPublicKeys);
    }

    private <T extends AccountPermissionResponse> T createAccountIntentId(Class<T> responseType) {
        // Account Permissions are added to persistentStorage
        Set<String> accountPermissions = new HashSet<>(ACCOUNT_PERMISSIONS);
        if (Objects.nonNull(aisConfig.getAdditionalPermissions())) {
            accountPermissions.addAll(aisConfig.getAdditionalPermissions());
        }

        persistentStorage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_PERMISSIONS_GRANTED,
                accountPermissions);

        return createAisRequest(aisConfig.createConsentRequestURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create(aisConfig.getAdditionalPermissions()))
                .post(responseType);
    }

    public List<AccountEntity> fetchV31Accounts() {
        return createAisRequest(aisConfig.getBulkAccountRequestURL())
                .get(AccountsV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public List<AccountBalanceEntity> fetchV31AccountBalances(String accountId) {
        return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId))
                .get(AccountBalanceV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public List<TrustedBeneficiaryEntity> fetchV31AccountBeneficiaries(String accountId) {
        return createAisRequest(aisConfig.getAccountBeneficiariesRequestURL(accountId))
                .get(TrustedBeneficiariesV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public Optional<IdentityDataV31Entity> fetchV31Party() {
        return executeV31FetchPartyRequest(
                createAisRequest(
                        aisConfig
                                .getApiBaseURL()
                                .concat(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY.getPath())));
    }

    public Optional<IdentityDataV31Entity> fetchV31Party(String accountId) {
        String path =
                String.format(
                        PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY.getPath(), accountId);
        return executeV31FetchPartyRequest(
                createAisRequest(aisConfig.getApiBaseURL().concat(path)));
    }

    private Optional<IdentityDataV31Entity> executeV31FetchPartyRequest(
            RequestBuilder requestBuilder) {
        try {
            return requestBuilder.get(PartyV31Response.class).getData();
        } catch (HttpResponseException ex) {
            checkForRestrictedDataForLastingConsentsError(ex);
            return Optional.empty();
        }
    }

    private void checkForRestrictedDataForLastingConsentsError(
            HttpResponseException responseException) {
        if (!new RestrictedDataForLastingConsentsErrorChecker(403)
                .isRestrictedDataLastingConsentsError(responseException)) {
            throw responseException;
        }
    }

    public List<IdentityDataV31Entity> fetchV31Parties(String accountId) {
        try {
            String path =
                    String.format(
                            PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES.getPath(),
                            accountId);
            return createAisRequest(aisConfig.getApiBaseURL().concat(path))
                    .get(PartiesV31Response.class)
                    .getData()
                    .orElse(Collections.emptyList());
        } catch (HttpResponseException ex) {
            if (!new RestrictedDataForLastingConsentsErrorChecker(401)
                    .isRestrictedDataLastingConsentsError(ex)) {
                throw ex;
            }
        }
        return Collections.emptyList();
    }

    public <T> T fetchAccountBalance(String accountId, Class<T> responseType) {
        return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId)).get(responseType);
    }

    public <T> T fetchAccountTransactions(String paginationKey, Class<T> responseType) {

        // Check if the key provided is a complete url or if it should be appended on the apiBase
        URL url = new URL(paginationKey);
        if (url.getScheme() == null) url = aisConfig.getApiBaseURL().concat(paginationKey);

        return createAisRequest(url).get(responseType);
    }

    public <T> T fetchUpcomingTransactions(String accountId, Class<T> responseType) {
        try {

            return createAisRequest(aisConfig.getUpcomingTransactionRequestURL(accountId))
                    .get(responseType);
        } catch (Exception e) {
            // TODO: Ukob testdata has an error in it which makes some transactions impossible to
            // parse.
            // TODO: This combined with the null check in UpcomingTransactionFetcher discards those
            // transactions to prevents crash.
            return null;
        }
    }

    private RequestBuilder createAisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(this.aisAuthFilter);
    }

    public String fetchIntentIdString() {
        return aisConfig.getIntentId(
                this.createAccountIntentId(aisConfig.getIntentIdResponseType()));
    }

    // General Payments Interface

    private RequestBuilder createPisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(this.pisAuthFilter)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8));
    }

    private RequestBuilder createPisRequestWithJwsHeader(URL url, Object request) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(this.pisAuthFilter)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8))
                .header(HttpHeaders.X_JWS_SIGNATURE, createJWTSignature(request));
    }

    @SuppressWarnings("unchecked")
    private String createJWTSignature(Object request) {

        String preferredAlgorithm =
                getWellKnownConfiguration()
                        .getPreferredIdTokenSigningAlg(
                                UkOpenBankingV31Constants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> requestBody = oMapper.convertValue(request, Map.class);

        ImmutableMap<String, Object> payloadClaims =
                ImmutableMap.<String, Object>builder()
                        .put(PAYLOAD.DATA, requestBody.get(PAYLOAD.DATA))
                        .put(PAYLOAD.RISK, requestBody.get(PAYLOAD.RISK))
                        .build();

        switch (UkOpenBankingV31Constants.SIGNING_ALGORITHM.valueOf(preferredAlgorithm)) {
            case PS256:
                return createPs256Signature(payloadClaims);
            case RS256:
            default:
                return createRs256Signature(payloadClaims);
        }
    }

    private String createRs256Signature(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();
        jwtHeaders.put(HEADERS.B64, false);
        jwtHeaders.put(HEADERS.IAT, new Date().getTime() - 1000);
        jwtHeaders.put(HEADERS.ISS, softwareStatement.getSoftwareId());
        jwtHeaders.put(HEADERS.CRIT, Arrays.asList(HEADERS.B64, HEADERS.IAT, HEADERS.ISS));

        return signer.sign(Algorithm.RS256, jwtHeaders, payloadClaims, true);
    }

    private String createPs256Signature(Map<String, Object> payloadClaims) {
        // Refer : https://openbanking.atlassian.net/wiki/spaces/DZ/pages/1112670669/W007
        // remove this check once this wavier times out
        if (POST_BASE64_GROUP.contains(aisConfig.getOrganisationId())) {
            return createPs256SignatureWithoutB64Header(payloadClaims);
        } else if (isHSBCFamily()) {
            return createHsbcFamilyHeader(payloadClaims);
        } else {
            return createPs256SignatureWithB64Header(payloadClaims);
        }
    }

    /**
     * HSBC and First Direct is under the same platform
     *
     * @return whether the bank is HSBC or First Direct
     */
    private boolean isHSBCFamily() {
        return HSBC_ORG_ID.equals(aisConfig.getOrganisationId());
    }

    private String createPs256SignatureWithB64Header(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(HEADERS.B64, false);
        jwtHeaders.put(HEADERS.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(
                HEADERS.ISS,
                String.format("%s/%s", TINK_UK_OPEN_BANKING_ORG_ID, GENERAL_STANDARD_ISS));
        jwtHeaders.put(HEADERS.TAN, UKOB_TAN);
        jwtHeaders.put(
                HEADERS.CRIT, Arrays.asList(HEADERS.B64, HEADERS.IAT, HEADERS.ISS, HEADERS.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    private String createPs256SignatureWithoutB64Header(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(HEADERS.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(
                HEADERS.ISS,
                String.format("%s/%s", TINK_UK_OPEN_BANKING_ORG_ID, GENERAL_STANDARD_ISS));
        jwtHeaders.put(HEADERS.TAN, UKOB_TAN);
        jwtHeaders.put(HEADERS.CRIT, Arrays.asList(HEADERS.IAT, HEADERS.ISS, HEADERS.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    private String createHsbcFamilyHeader(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(HEADERS.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(HEADERS.ISS, RFC_2253_DN);
        jwtHeaders.put(HEADERS.TAN, UKOB_TAN);
        jwtHeaders.put(HEADERS.CRIT, Arrays.asList(HEADERS.IAT, HEADERS.ISS, HEADERS.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    public <T> T createDomesticPaymentConsent(Object request, Class<T> responseType) {
        return createPisRequestWithJwsHeader(pisConfig.createDomesticPaymentConsentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticPaymentConsent(String consentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getDomesticPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T executeDomesticPayment(Object request, Class<T> responseType) {
        return createPisRequestWithJwsHeader(pisConfig.createDomesticPaymentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticFundsConfirmation(String consentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getDomesticFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T getDomesticPayment(String paymentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getDomesticPayment(paymentId)).get(responseType);
    }

    public <T> T createDomesticScheduledPaymentConsent(Object request, Class<T> responseType) {
        return createPisRequestWithJwsHeader(
                        pisConfig.createDomesticScheduledPaymentConsentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticScheduledPaymentConsent(String consentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getDomesticScheduledPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T executeDomesticScheduledPayment(Object request, Class<T> responseType) {
        return createPisRequestWithJwsHeader(pisConfig.createDomesticScheduledPaymentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticScheduledPayment(String paymentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getDomesticScheduledPayment(paymentId)).get(responseType);
    }

    public <T> T createInternationalPaymentConsent(Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createInternationalPaymentConsentURL())
                .post(responseType, request);
    }

    public <T> T getInternationalPaymentConsent(String consentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getInternationalPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T getInternationalPayment(String paymentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getInternationalPayment(paymentId))
                .post(responseType, paymentId);
    }

    public <T> T getInternationalFundsConfirmation(String consentId, Class<T> responseType) {
        return createPisRequest(pisConfig.getInternationalFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T executeInternationalPayment(Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createInternationalPaymentURL())
                .post(responseType, request);
    }

    public WellKnownResponse getWellKnownConfiguration() {
        if (Objects.nonNull(cachedWellKnownResponse)) {
            return cachedWellKnownResponse;
        }

        /*
         * Regarding the well-known URL endpoint, some bank APIs (such as FirstDirect) sends
         * response with wrong MIME type (such as octet-stream). If we want to cast the response
         * payload into WellKnownResponse directly, we fail as TinkHttpClient does not know how to
         * handle application/octet-stream in this case. For this reason, we cast the response
         * payload into string first and then serialize it by using SerializationUtils class
         */
        String response = httpClient.request(wellKnownURL).get(String.class);

        cachedWellKnownResponse =
                SerializationUtils.deserializeFromString(response, WellKnownResponse.class);

        return cachedWellKnownResponse;
    }

    OAuth2Token requestClientCredentials(ClientMode scope) {
        TokenRequestForm postData = createTokenRequestForm("client_credentials", scope);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken, ClientMode scope) {
        TokenRequestForm postData =
                createTokenRequestForm("refresh_token", scope).withRefreshToken(refreshToken);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    OAuth2Token exchangeAccessCode(String code) {
        TokenRequestForm postData = createTokenRequestFormWithoutScope().withCode(code);

        return createTokenRequest().body(postData).post(TokenResponse.class).toAccessToken();
    }

    private void addFilter(Filter filter) {
        httpClient.addFilter(filter);
    }

    private TokenRequestForm createTokenRequestFormWithoutScope() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType("authorization_code")
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    protected void handleFormAuthentication(
            TokenRequestForm requestForm, WellKnownResponse wellKnownConfiguration) {
        final TokenEndpointAuthMethod authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellKnownConfiguration);

        switch (authMethod) {
            case CLIENT_SECRET_POST:
                requestForm.withClientSecretPost(
                        providerConfiguration.getClientId(),
                        providerConfiguration.getClientSecret());
                break;

            case PRIVATE_KEY_JWT:
                requestForm.withPrivateKeyJwt(
                        signer, wellKnownConfiguration, providerConfiguration);
                break;

            case CLIENT_SECRET_BASIC:
                // Add to header.
                break;

            case TLS_CLIENT_AUTH:
                // Do nothing. We authenticate using client certificate.
                requestForm.withClientId(providerConfiguration.getClientId());
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }
    }

    private TokenRequestForm createTokenRequestForm(String grantType, ClientMode mode) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        // Token request does not use OpenId scope
        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(Collections.singletonList(mode.getValue()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        TokenRequestForm requestForm =
                new TokenRequestForm()
                        .withGrantType(grantType)
                        .withScope(scope)
                        .withRedirectUri(redirectUrl);

        handleFormAuthentication(requestForm, wellKnownConfiguration);

        return requestForm;
    }

    protected RequestBuilder createTokenRequest() {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        RequestBuilder requestBuilder =
                httpClient
                        .request(wellKnownConfiguration.getTokenEndpoint())
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        TokenEndpointAuthMethod authMethod =
                determineTokenEndpointAuthMethod(providerConfiguration, wellKnownConfiguration);

        switch (authMethod) {
            case CLIENT_SECRET_BASIC:
                // `client_secret_basic` does not add data to the body, but on the header.
                requestBuilder =
                        requestBuilder.addBasicAuth(
                                providerConfiguration.getClientId(),
                                providerConfiguration.getClientSecret());
                break;
            case TLS_CLIENT_AUTH:
                break;

            case PRIVATE_KEY_JWT:
                // Add to header.
                break;

            case CLIENT_SECRET_POST:
                // Do nothing. We authenticate using client certificate.
                break;

            default:
                throw new IllegalStateException(
                        String.format(
                                "Not yet implemented auth method: %s", authMethod.toString()));
        }

        return requestBuilder;
    }

    private TokenEndpointAuthMethod determineTokenEndpointAuthMethod(
            ClientInfo clientInfo, WellKnownResponse wellKnownConfiguration) {

        if (!Strings.isNullOrEmpty(clientInfo.getTokenEndpointAuthMethod())) {
            return TokenEndpointAuthMethod.valueOf(
                    clientInfo.getTokenEndpointAuthMethod().toUpperCase());
        }

        return wellKnownConfiguration
                .getPreferredTokenEndpointAuthMethod(
                        UkOpenBankingV31Constants.PREFERRED_TOKEN_ENDPOINT_AUTH_METHODS)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Preferred token endpoint auth method not found."));
    }

    public URL buildAuthorizeUrl(
            String state, String nonce, ClientMode mode, String callbackUri, URL authEndpoint) {
        WellKnownResponse wellKnownConfiguration = getWellKnownConfiguration();

        String responseType = String.join(" ", UkOpenBankingV31Constants.MANDATORY_RESPONSE_TYPES);

        String scope =
                wellKnownConfiguration
                        .verifyAndGetScopes(
                                Arrays.asList(
                                        UkOpenBankingV31Constants.Scopes.OPEN_ID, mode.getValue()))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Provider does not support the mandatory scopes."));

        String redirectUri =
                Optional.ofNullable(callbackUri).filter(s -> !s.isEmpty()).orElse(redirectUrl);

        URL authorizationEndpoint =
                Optional.ofNullable(authEndpoint)
                        .orElse(wellKnownConfiguration.getAuthorizationEndpoint());

        /*  'response_type=id_token' only supports 'response_mode=fragment',
         *  setting 'response_mode=query' has no effect the the moment.
         */
        return authorizationEndpoint
                .queryParam(UkOpenBankingV31Constants.Params.RESPONSE_TYPE, responseType)
                .queryParam(
                        UkOpenBankingV31Constants.Params.CLIENT_ID,
                        providerConfiguration.getClientId())
                .queryParam(UkOpenBankingV31Constants.Params.SCOPE, scope)
                .queryParam(UkOpenBankingV31Constants.Params.STATE, state)
                .queryParam(UkOpenBankingV31Constants.Params.NONCE, nonce)
                .queryParam(UkOpenBankingV31Constants.Params.REDIRECT_URI, redirectUri);
    }
}
