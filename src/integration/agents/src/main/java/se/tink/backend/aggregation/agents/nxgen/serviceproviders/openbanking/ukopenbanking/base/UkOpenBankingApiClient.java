package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants.ACCOUNT_PERMISSIONS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints.IDENTITY_DATA_ENDPOINT_PARTY;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.MONZO_ORG_ID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.TINK_UKOPENBANKING_ORGID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.UKOB_TAN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UkOpenBankingApiClient extends OpenIdApiClient {

    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenBankingAisConfig aisConfig;

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            ProviderConfiguration providerConfiguration,
            URL wellKnownURL,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig) {
        super(
                httpClient,
                signer,
                softwareStatement,
                providerConfiguration,
                wellKnownURL,
                randomValueGenerator);
        this.persistentStorage = persistentStorage;
        this.randomValueGenerator = randomValueGenerator;
        this.aisConfig = aisConfig;
    }

    public <T> T createPaymentIntentId(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createPaymentsURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T> T submitPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createPaymentSubmissionURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    private <T extends AccountPermissionResponse> T createAccountIntentId(Class<T> responseType) {
        // Account Permissions are added to persistentStorage
        List<String> accountPermissions = new ArrayList<>(ACCOUNT_PERMISSIONS);
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
        return createAisRequest(aisConfig.getApiBaseURL().concat(IDENTITY_DATA_ENDPOINT_PARTY))
                .get(PartyV31Response.class)
                .getData();
    }

    public Optional<IdentityDataV31Entity> fetchV31Party(String accountId) {
        String path = String.format(IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY, accountId);
        return createAisRequest(aisConfig.getApiBaseURL().concat(path))
                .get(PartyV31Response.class)
                .getData();
    }

    public List<IdentityDataV31Entity> fetchV31Parties(String accountId) {
        String path = String.format(IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES, accountId);
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

    protected RequestBuilder createPisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getPisAuthFilter());
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

    private RequestBuilder createPISRequest(URL url) {
        return createPisRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8));
    }

    private RequestBuilder createPISRequestWithJWSHeader(URL url, Object request) {
        return createPisRequest(url)
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
        // Monzo does not work with B64 header hence creating signature with out B64 for now.
        // Refer : https://openbanking.atlassian.net/wiki/spaces/DZ/pages/1112670669/W007
        // remove this check once this wavier times out
        // Monzo organization ID check
        if (MONZO_ORG_ID.equals(providerConfiguration.getOrganizationId())) {
            return createPs256SignatureWithoutB64Header(payloadClaims);
        } else {
            return createPs256SignatureWithB64Header(payloadClaims);
        }
    }

    private String createPs256SignatureWithB64Header(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(HEADERS.B64, false);
        jwtHeaders.put(HEADERS.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(
                HEADERS.ISS,
                new StringBuilder(TINK_UKOPENBANKING_ORGID)
                        .append("/")
                        .append(softwareStatement.getSoftwareId())
                        .toString());
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
                new StringBuilder(TINK_UKOPENBANKING_ORGID)
                        .append("/")
                        .append(softwareStatement.getSoftwareId())
                        .toString());
        jwtHeaders.put(HEADERS.TAN, UKOB_TAN);
        jwtHeaders.put(HEADERS.CRIT, Arrays.asList(HEADERS.IAT, HEADERS.ISS, HEADERS.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    public <T> T createDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequestWithJWSHeader(pisConfig.createDomesticPaymentConsentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T executeDomesticPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequestWithJWSHeader(pisConfig.createDomesticPaymentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticFundsConfirmation(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T getDomesticPayment(
            UkOpenBankingPisConfig pisConfig, String paymentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPayment(paymentId)).get(responseType);
    }

    public <T> T createInternationalPaymentConsent(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentConsentURL())
                .post(responseType, request);
    }

    public <T> T getInternationalPaymentConsent(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T getInternationalPayment(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalPayment(consentId))
                .post(responseType, consentId);
    }

    public <T> T getInternationalFundsConfirmation(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getInternationalFundsConfirmationURL(consentId))
                .get(responseType);
    }

    public <T> T executeInternationalPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createInternationalPaymentURL())
                .post(responseType, request);
    }
}
