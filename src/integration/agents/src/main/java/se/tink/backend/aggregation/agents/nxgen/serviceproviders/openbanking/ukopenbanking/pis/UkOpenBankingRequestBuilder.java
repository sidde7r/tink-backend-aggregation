package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner.Algorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.JWTSignatureHeaders.Headers;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentConstants.JWTSignatureHeaders.Payload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UkOpenBankingRequestBuilder {

    private static final List<String> POST_BASE64_GROUP =
            Arrays.asList(
                    UkOpenBankingV31PaymentConstants.DANSKEBANK_ORG_ID,
                    UkOpenBankingV31PaymentConstants.MONZO_ORG_ID,
                    UkOpenBankingV31PaymentConstants.NATIONWIDE_ORG_ID,
                    UkOpenBankingV31PaymentConstants.ULSTER_ORG_ID,
                    UkOpenBankingV31PaymentConstants.RBS_ORG_ID,
                    UkOpenBankingV31PaymentConstants.NATWEST_ORG_ID,
                    UkOpenBankingV31PaymentConstants.BARCLAYS_ORG_ID);

    private final TinkHttpClient httpClient;
    private final UkOpenBankingPisAuthApiClient authApiClient;
    private final JwtSigner signer;
    private final SoftwareStatementAssertion softwareStatement;
    private final RandomValueGenerator randomValueGenerator;
    private final UkOpenBankingPisConfig pisConfig;

    public RequestBuilder createPisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(authApiClient.getPisAuthFilter())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8));
    }

    public RequestBuilder createPisRequestWithJwsHeader(URL url, Object request) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(authApiClient.getPisAuthFilter())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .header(
                        HttpHeaders.X_IDEMPOTENCY_KEY,
                        randomValueGenerator.generateRandomHexEncoded(8))
                .header(HttpHeaders.X_JWS_SIGNATURE, createJWTSignature(request));
    }

    @SuppressWarnings("unchecked")
    private String createJWTSignature(Object request) {
        String preferredAlgorithm =
                authApiClient
                        .getWellKnownConfiguration()
                        .getPreferredIdTokenSigningAlg(
                                UkOpenBankingV31PaymentConstants
                                        .PREFERRED_ID_TOKEN_SIGNING_ALGORITHM)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Preferred signing algorithm unknown: only RS256 and PS256 are supported"));

        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> requestBody = oMapper.convertValue(request, Map.class);

        ImmutableMap<String, Object> payloadClaims =
                ImmutableMap.<String, Object>builder()
                        .put(Payload.DATA, requestBody.get(Payload.DATA))
                        .put(Payload.RISK, requestBody.get(Payload.RISK))
                        .build();

        switch (UkOpenBankingV31PaymentConstants.SIGNING_ALGORITHM.valueOf(preferredAlgorithm)) {
            case PS256:
                return createPs256Signature(payloadClaims);
            case RS256:
            default:
                return createRs256Signature(payloadClaims);
        }
    }

    private String createRs256Signature(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();
        jwtHeaders.put(Headers.B64, false);
        jwtHeaders.put(Headers.IAT, new Date().getTime() - 1000);
        jwtHeaders.put(Headers.ISS, softwareStatement.getSoftwareId());
        jwtHeaders.put(Headers.CRIT, Arrays.asList(Headers.B64, Headers.IAT, Headers.ISS));

        return signer.sign(Algorithm.RS256, jwtHeaders, payloadClaims, true);
    }

    private String createPs256Signature(Map<String, Object> payloadClaims) {
        // Refer : https://openbanking.atlassian.net/wiki/spaces/DZ/pages/1112670669/W007
        // remove this check once this wavier times out
        if (POST_BASE64_GROUP.contains(pisConfig.getOrganisationId())) {
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
        return UkOpenBankingV31PaymentConstants.HSBC_ORG_ID.equals(pisConfig.getOrganisationId());
    }

    private String createPs256SignatureWithB64Header(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(Headers.B64, false);
        jwtHeaders.put(Headers.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(
                Headers.ISS,
                String.format(
                        "%s/%s",
                        UkOpenBankingV31PaymentConstants.TINK_UK_OPEN_BANKING_ORG_ID,
                        UkOpenBankingV31PaymentConstants.GENERAL_STANDARD_ISS));
        jwtHeaders.put(Headers.TAN, UkOpenBankingV31PaymentConstants.UKOB_TAN);
        jwtHeaders.put(
                Headers.CRIT, Arrays.asList(Headers.B64, Headers.IAT, Headers.ISS, Headers.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    private String createPs256SignatureWithoutB64Header(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(Headers.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(
                Headers.ISS,
                String.format(
                        "%s/%s",
                        UkOpenBankingV31PaymentConstants.TINK_UK_OPEN_BANKING_ORG_ID,
                        UkOpenBankingV31PaymentConstants.GENERAL_STANDARD_ISS));
        jwtHeaders.put(Headers.TAN, UkOpenBankingV31PaymentConstants.UKOB_TAN);
        jwtHeaders.put(Headers.CRIT, Arrays.asList(Headers.IAT, Headers.ISS, Headers.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }

    private String createHsbcFamilyHeader(Map<String, Object> payloadClaims) {

        Map<String, Object> jwtHeaders = new LinkedHashMap<>();

        jwtHeaders.put(Headers.IAT, Instant.now().minusSeconds(3600).getEpochSecond());
        jwtHeaders.put(Headers.ISS, UkOpenBankingV31PaymentConstants.RFC_2253_DN);
        jwtHeaders.put(Headers.TAN, UkOpenBankingV31PaymentConstants.UKOB_TAN);
        jwtHeaders.put(Headers.CRIT, Arrays.asList(Headers.IAT, Headers.ISS, Headers.TAN));

        return signer.sign(Algorithm.PS256, jwtHeaders, payloadClaims, true);
    }
}
