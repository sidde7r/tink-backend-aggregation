package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.collect.Maps;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JWTUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class RedsysApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private RedsysConfiguration configuration;

    public RedsysApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(RedsysConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public boolean hasValidAccessToken() {
        try {
            return getTokenFromStorage().isValid();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private String makeAuthUrl(String path) {
        assert path.startsWith("/");
        return String.format(
                "%s/%s%s",
                getConfiguration().getBaseAuthUrl(), getConfiguration().getAspsp(), path);
    }

    private String makeApiUrl(String path) {
        assert path.startsWith("/");
        return String.format(
                "%s/%s%s", getConfiguration().getBaseAPIUrl(), getConfiguration().getAspsp(), path);
    }

    public URL getAuthorizeUrl(String state, String codeChallenge) {
        final String clientId = getConfiguration().getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return client.request(makeAuthUrl(RedsysConstants.Urls.OAUTH))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    public OAuth2Token getToken(String code, String codeVerifier) {
        final String clientId = getConfiguration().getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.REDIRECT_URI, redirectUri)
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .build()
                        .serialize();

        final OAuth2Token token =
                client.request(makeAuthUrl(Urls.TOKEN))
                        .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class)
                        .toTinkToken();
        return token;
    }

    public Pair<String, URL> requestConsent() {
        final String url = makeApiUrl(Urls.CONSENTS);
        final GetConsentRequest getConsentRequest =
                new GetConsentRequest(
                        new AccessEntity(null, null, null, null, FormValues.ALL_ACCOUNTS),
                        FormValues.TRUE,
                        FormValues.VALID_UNTIL,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.FALSE);

        final GetConsentResponse getConsentResponse =
                createSignedRequest(url, getConsentRequest)
                        .headers(getConsentTppRedirectHeaders())
                        .post(GetConsentResponse.class);
        final String consentId = getConsentResponse.getConsentId();
        final String consentRedirectUrl =
                getConsentResponse
                        .getLink(RedsysConstants.Links.SCA_REDIRECT)
                        .map(LinkEntity::getHref)
                        .get();
        return new Pair<>(consentId, new URL(consentRedirectUrl));
    }

    private String getConsentId() {
        return sessionStorage.get(StorageKeys.CONSENT_ID);
    }

    public String fetchConsentStatus(String consentId) {
        final String url = makeApiUrl(String.format(Urls.CONSENT_STATUS, consentId));
        final ConsentStatusResponse consentStatusResponse =
                createSignedRequest(url)
                        .headers(getConsentTppRedirectHeaders())
                        .get(ConsentStatusResponse.class);
        return consentStatusResponse.getConsentStatus();
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String url = getConfiguration().getBaseAuthUrl() + "/" + Urls.REFRESH;
        final String aspsp = getConfiguration().getAspsp();
        final String clientId = getConfiguration().getAuthClientId();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.ASPSP, aspsp)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return client.request(url)
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private X509Certificate getCertificate() {
        try {
            InputStream in =
                    new FileInputStream(getConfiguration().getClientSigningCertificatePath());
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(in);
            return cert;
        } catch (IOException | CertificateException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String getKeyID(X509Certificate cert) {
        return String.format(
                Locale.ENGLISH,
                Signature.KEY_ID_FORMAT,
                cert.getSerialNumber(),
                cert.getIssuerX500Principal().getName());
    }

    private String generateRequestSignature(
            String digest, String requestID, String tppRedirectUri) {
        String payloadToSign =
                String.format(
                        "%s: %s\n%s: %s",
                        HeaderKeys.DIGEST.toLowerCase(Locale.ENGLISH),
                        digest,
                        HeaderKeys.REQUEST_ID.toLowerCase(Locale.ENGLISH),
                        requestID);
        String headers = HeaderKeys.DIGEST + " " + HeaderKeys.REQUEST_ID;
        if (!Strings.isNullOrEmpty(tppRedirectUri)) {
            headers += " " + HeaderKeys.TPP_REDIRECT_URI;
            payloadToSign +=
                    String.format(
                            "\n%s: %s",
                            HeaderKeys.TPP_REDIRECT_URI.toLowerCase(Locale.ENGLISH),
                            tppRedirectUri);
        }

        final String keyPath = getConfiguration().getClientSigningKeyPath();
        final PrivateKey privateKey = JWTUtils.readSigningKey(keyPath, Signature.KEY_ALGORITHM);
        final String signature =
                Base64.getEncoder()
                        .encodeToString(RSA.signSha256(privateKey, payloadToSign.getBytes()));
        final String keyID = getKeyID(getCertificate());

        return String.format(
                Signature.FORMAT, keyID, headers.toLowerCase(Locale.ENGLISH), signature);
    }

    private RequestBuilder createSignedRequest(String url, @Nullable Object payload) {
        return createSignedRequest(url, payload, getTokenFromStorage());
    }

    private RequestBuilder createSignedRequest(String url) {
        return createSignedRequest(url, null, getTokenFromStorage());
    }

    private Map<String, Object> getConsentTppRedirectHeaders() {
        Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TRUE);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getConsentRedirectUrl());
        headers.put(HeaderKeys.TPP_NOK_REDIRECT_URI, getConfiguration().getConsentRedirectUrl());
        return headers;
    }

    private RequestBuilder createSignedRequest(
            String url, @Nullable Object payload, OAuth2Token token) {
        String serializedPayload = "";
        if (payload != null) {
            serializedPayload = SerializationUtils.serializeToString(payload);
        }
        final String digest =
                Signature.DIGEST_PREFIX
                        + Base64.getEncoder().encodeToString(Hash.sha256(serializedPayload));
        final String requestID = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
        final String signature = generateRequestSignature(digest, requestID, null);
        final String encodedCertificate;
        try {
            encodedCertificate = Base64.getEncoder().encodeToString(getCertificate().getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        RequestBuilder request =
                client.request(url)
                        .addBearerToken(token)
                        .header(HeaderKeys.IBM_CLIENT_ID, getConfiguration().getClientId())
                        .header(HeaderKeys.DIGEST, digest)
                        .header(HeaderKeys.REQUEST_ID, requestID)
                        .header(HeaderKeys.SIGNATURE, signature)
                        .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, encodedCertificate);

        if (payload != null) {
            request = request.body(serializedPayload, MediaType.APPLICATION_JSON);
        }

        return request;
    }

    public ListAccountsResponse fetchAccounts() {
        final String consentId = getConsentId();
        return createSignedRequest(makeApiUrl(Urls.ACCOUNTS))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(ListAccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, @Nullable String link) {
        final String consentId = getConsentId();
        final String path =
                Optional.ofNullable(link).orElse(String.format(Urls.TRANSACTIONS, accountId));
        RequestBuilder request =
                createSignedRequest(makeApiUrl(path))
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BookingStatus.BOTH);

        return request.get(TransactionsResponse.class);
    }
}
