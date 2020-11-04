package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants.ACCOUNT_PERMISSIONS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.BARCLAYS_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.DANSKEBANK_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.GENERAL_STANDARD_ISS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.HSBC_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.MONZO_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.NATIONWIDE_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.NATWEST_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.RBS_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.RFC_2253_DN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.TINK_UKOPENBANKING_ORGID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.UKOB_TAN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ULSTER_ORG_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
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
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.FinancialOrganisationIdFilter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UkOpenBankingApiClient extends OpenIdApiClient {

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
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                aisConfig.getWellKnownURL(),
                randomValueGenerator);
        this.persistentStorage = persistentStorage;
        this.randomValueGenerator = randomValueGenerator;
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;
        addFilter(new ServiceUnavailableBankServiceErrorFilter());
        addFilter(new FinancialOrganisationIdFilter(aisConfig.getOrganisationId()));
    }

    private <T extends AccountPermissionResponse> T createAccountIntentId(Class<T> responseType) {
        // Account Permissions are added to persistentStorage
        Set<String> accountPermissions = new HashSet<>(ACCOUNT_PERMISSIONS);
        if (Objects.nonNull(aisConfig.getAdditionalPermissions())) {
            accountPermissions.addAll(aisConfig.getAdditionalPermissions());
        }

        persistentStorage.put(
                PersistentStorageKeys.AIS_ACCOUNT_PERMISSIONS_GRANTED, accountPermissions);

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
        return createAisRequest(
                        aisConfig
                                .getApiBaseURL()
                                .concat(PartyEndpoint.IDENTITY_DATA_ENDPOINT_PARTY.getPath()))
                .get(PartyV31Response.class)
                .getData();
    }

    public Optional<IdentityDataV31Entity> fetchV31Party(String accountId) {
        String path =
                String.format(
                        PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY.getPath(), accountId);
        return createAisRequest(aisConfig.getApiBaseURL().concat(path))
                .get(PartyV31Response.class)
                .getData();
    }

    public List<IdentityDataV31Entity> fetchV31Parties(String accountId) {
        String path =
                String.format(
                        PartyEndpoint.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES.getPath(),
                        accountId);
        return createAisRequest(aisConfig.getApiBaseURL().concat(path))
                .get(PartiesV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
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

    public RequestBuilder createAisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getAisAuthFilter());
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
                .addFilter(getPisAuthFilter())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8));
    }

    private RequestBuilder createPisRequestWithJwsHeader(URL url, Object request) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getPisAuthFilter())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8))
                .header(HttpHeaders.X_JWS_SIGNATURE, createJWTSignature(request));
    }

    private String createJWTSignature(Object request) {

        String preferredAlgorithm =
                getWellKnownConfiguration()
                        .getPreferredIdTokenSigningAlg(
                                OpenIdConstants.PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
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

        switch (OpenIdConstants.SIGNING_ALGORITHM.valueOf(preferredAlgorithm)) {
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
                String.format("%s/%s", TINK_UKOPENBANKING_ORGID, GENERAL_STANDARD_ISS));
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
                String.format("%s/%s", TINK_UKOPENBANKING_ORGID, GENERAL_STANDARD_ISS));
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
}
