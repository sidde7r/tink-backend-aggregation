package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.TINK_UKOPENBANKING_ORGID;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.UKOB_TAN;

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.TinkJwtCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.JWTSignatureHeaders.HEADERS;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.JWTSignatureHeaders.PAYLOAD;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.utils.crypto.PS256;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.OpenIdSignUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingApiClient extends OpenIdApiClient {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(UkOpenBankingApiClient.class);

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            URL wellKnownURL) {
        super(httpClient, softwareStatement, providerConfiguration, wellKnownURL);
    }

    public <T> T createPaymentIntentId(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createPaymentsURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    public <T> T submitPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPisRequest(pisConfig.createPaymentSubmissionURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .body(request)
                .post(responseType);
    }

    private <T extends AccountPermissionResponse> T createAccountIntentId(
            UkOpenBankingAisConfig aisConfig, Class<T> responseType) {

        return createAisRequest(aisConfig.createConsentRequestURL())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .body(AccountPermissionRequest.create())
                .post(responseType);
    }

    public <T> T fetchAccounts(UkOpenBankingAisConfig aisConfig, Class<T> responseType) {
        return createAisRequest(aisConfig.getBulkAccountRequestURL()).get(responseType);
    }

    public <T> T fetchAccountBalance(
            UkOpenBankingAisConfig aisConfig, String accountId, Class<T> responseType) {
        return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId)).get(responseType);
    }

    public <T> T fetchAccountTransactions(
            UkOpenBankingAisConfig aisConfig, String paginationKey, Class<T> responseType) {

        // Check if the key provided is a complete url or if it should be appended on the apiBase
        URL url = new URL(paginationKey);
        if (url.getScheme() == null) url = aisConfig.getApiBaseURL().concat(paginationKey);

        return createAisRequest(url).get(responseType);
    }

    public <T> T fetchUpcomingTransactions(
            UkOpenBankingAisConfig aisConfig, String accountId, Class<T> responseType) {
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

    private RequestBuilder createPisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getPisAuthFilter());
    }

    private RequestBuilder createAisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getAisAuthFilter());
    }

    public String fetchIntentIdString(UkOpenBankingAisConfig aisConfig) {
        return aisConfig.getIntentId(
                this.createAccountIntentId(aisConfig, aisConfig.getIntentIdResponseType()));
    }

    // General Payments Interface

    private RequestBuilder createPISRequest(URL url) {
        return createPisRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8));
    }

    private RequestBuilder createPISRequest(URL url, Object request) {
        return createPisRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        UkOpenBankingConstants.HttpHeaders.X_IDEMPOTENCY_KEY,
                        RandomUtils.generateRandomHexEncoded(8))
                .header(HttpHeaders.X_JWS_SIGNATURE, createJWTSignature(request));
    }

    private String createJWTSignature(Object request) {
        String keyId = softwareStatement.getSigningKeyId();
        String jwsAlgorithm = null;
        try {
            jwsAlgorithm =
                    SignedJWT.parse(softwareStatement.getAssertion())
                            .getHeader()
                            .getAlgorithm()
                            .getName();
        } catch (ParseException e) {
            LOGGER.error(
                    "Not able to parse algorithm from Software Statement so defaulting to RS256. "
                            + "This should never happen. "
                            + Arrays.toString(e.getStackTrace()));
        }

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> payloadClaims = oMapper.convertValue(request, Map.class);

        switch (OpenIdConstants.SIGNING_ALGORITHM.valueOf(jwsAlgorithm)) {
            case PS256:
                return createPs256Signature(keyId, payloadClaims);
            case RS256:
            default:
                return createRs256Signature(keyId, payloadClaims);
        }
    }

    private String createRs256Signature(String keyId, Map<String, Object> payloadClaims) {

        Algorithm algorithm =
                OpenIdSignUtils.getSignatureAlgorithm(softwareStatement.getSigningKey());

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();
        jwtHeaders.put(HEADERS.KID, keyId);
        jwtHeaders.put(HEADERS.B64, false);
        jwtHeaders.put(HEADERS.IAT, new Date().getTime() - 1000);
        jwtHeaders.put(HEADERS.ISS, softwareStatement.getSoftwareId());
        String[] crit = {HEADERS.B64, HEADERS.IAT, HEADERS.ISS};
        jwtHeaders.put(HEADERS.CRIT, crit);

        String signature =
                TinkJwtCreator.create()
                        .withHeader(jwtHeaders)
                        .withClaim(PAYLOAD.DATA, payloadClaims.get(PAYLOAD.DATA))
                        .withClaim(PAYLOAD.RISK, payloadClaims.get(PAYLOAD.RISK))
                        .sign(algorithm);

        String[] jwtParts = signature.split("\\.");
        return jwtParts[0] + ".." + jwtParts[2];
    }

    private String createPs256Signature(String keyId, Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(HEADERS.B64, false);
        jwtHeaders.put(HEADERS.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(HEADERS.ISS, TINK_UKOPENBANKING_ORGID);
        jwtHeaders.put(HEADERS.TAN, UKOB_TAN);
        String[] crit = {HEADERS.B64, HEADERS.IAT, HEADERS.ISS, HEADERS.TAN};

        JWSHeader jwsHeader =
                new JWSHeader.Builder(JWSAlgorithm.PS256)
                        .keyID(keyId)
                        .criticalParams(new HashSet<>(Arrays.asList(crit)))
                        .customParams(jwtHeaders)
                        .build();

        JSONObject object = new JSONObject();
        object.put(PAYLOAD.RISK, payloadClaims.get(PAYLOAD.RISK));
        object.put(PAYLOAD.DATA, payloadClaims.get(PAYLOAD.DATA));

        return PS256.sign(jwsHeader, object, softwareStatement.getSigningKey(), true);
    }

    public <T> T createDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentConsentURL(), request)
                .post(responseType, request);
    }

    public <T> T getDomesticPaymentConsent(
            UkOpenBankingPisConfig pisConfig, String consentId, Class<T> responseType) {
        return createPISRequest(pisConfig.getDomesticPaymentConsentURL(consentId))
                .get(responseType);
    }

    public <T> T executeDomesticPayment(
            UkOpenBankingPisConfig pisConfig, Object request, Class<T> responseType) {
        return createPISRequest(pisConfig.createDomesticPaymentURL(), request)
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
