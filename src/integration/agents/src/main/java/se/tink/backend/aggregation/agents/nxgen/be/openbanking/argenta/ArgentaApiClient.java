package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.ScaSelectionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration.ArgentaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils.TimeUtils;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class ArgentaApiClient {

    private final TinkHttpClient client;
    private final ArgentaConfiguration configuration;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final EidasIdentity eidasIdentity;

    public ArgentaApiClient(
            TinkHttpClient client,
            ArgentaConfiguration configuration,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        Preconditions.checkNotNull(configuration);

        this.client = client;
        this.configuration = configuration;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, "");
    }

    private RequestBuilder createRequest(URL url, String requestBody) {
        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(requestBody);
        Map<String, Object> headers = getHeaders(requestId, digest);

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .headers(headers)
                .header(HeaderKeys.API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CERTIFICATE, configuration.getClientSigningCertificate())
                .header(HeaderKeys.PSU_ID_ADDRESS, configuration.getPsuIpAddress())
                .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .addBearerToken(getTokenFromStorage())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state, String consentId) {

        final String codeVerifier = Psd2Headers.generateCodeVerifier();
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);

        return createRequest(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.S256)
                .queryParam(QueryKeys.SCOPE, String.format(QueryValues.SCOPE, consentId))
                .queryParam(
                        QueryKeys.CODE_CHALLENGE, Psd2Headers.generateCodeChallenge(codeVerifier))
                .getUrl();
    }

    public ConsentResponse getConsent(List<IbanEntity> ibans) {
        ConsentRequest consentRequest =
                new ConsentRequest(
                        LocalDate.now().plusDays(FormValues.NUMBER_OF_VALID_DAYS).toString(),
                        false,
                        new ConsentRequestAccessEntity(ibans),
                        4);

        return createRequest(Urls.CONSENT, SerializationUtils.serializeToString(consentRequest))
                .post(ConsentResponse.class, consentRequest);
    }

    public void selectAuthenticationMethod(
            String selectAuthenticationMethodUrl, String authenticationMethodId) {

        ScaSelectionRequest scaSelectionRequest = new ScaSelectionRequest(authenticationMethodId);

        createRequest(
                        new URL(selectAuthenticationMethodUrl),
                        SerializationUtils.serializeToString(scaSelectionRequest))
                .type(HeaderValues.JSON_UTF_8)
                .patch(String.class, scaSelectionRequest);
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {

        TokenRequest tokenRequest =
                new TokenRequest(
                        code,
                        FormValues.AUTHORIZATION_CODE,
                        configuration.getClientId(),
                        configuration.getRedirectUrl(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return createRequest(Urls.TOKEN, tokenRequest.toData())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    public AccountResponse getAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(HeaderKeys.DATE, getFormattedDate())
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountResponse.class);
    }

    public TransactionsResponse getTransactions(String apiIdentifier, Date fromDate, Date toDate) {
        TransactionsResponse transactionsResponse = new TransactionsResponse();
        if (checkWithin90Days(toDate)) {
            transactionsResponse =
                    createRequestInSession(
                                    Urls.TRANSACTIONS.parameter(
                                            PathVariables.ACCOUNT_ID, apiIdentifier))
                            .header(HeaderKeys.DATE, getFormattedDate())
                            .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                            .queryParam(
                                    QueryKeys.DATE_FROM,
                                    ThreadSafeDateFormat.FORMATTER_DAILY.format(
                                            TimeUtils.get90DaysDate(toDate)))
                            .queryParam(
                                    QueryKeys.DATE_TO,
                                    ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                            .get(TransactionsResponse.class);
        }
        return transactionsResponse;
    }

    private String getFormattedDate() {
        return DateTimeFormatter.ofPattern(ArgentaConstants.Formats.HEADER_DATE_FORMAT)
                .format(ZonedDateTime.now(ZoneOffset.UTC));
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcSigner signer =
                QsealcSigner.build(
                        agentsServiceConfiguration.getEidasProxy().toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = signer.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                HeaderValues.SIGNATURE_HEADER, configuration.getKeyId(), signedHeaders, signature);
    }

    private String createDigest(String body) {
        return String.format(
                HeaderValues.SHA_256.concat("%s"),
                Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }

    private Map<String, Object> getHeaders(String requestId, String digest) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.DIGEST, digest);
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        return headers;
    }

    public OAuth2Token exchangeRefreshToken(String refreshToken) {

        RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(FormValues.REFRESH_TOKEN, refreshToken);

        return createRequest(Urls.TOKEN, refreshTokenRequest.toData())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshTokenRequest.toData())
                .toTinkToken();
    }

    private boolean checkWithin90Days(Date toDate) {
        Date last90DaysFromNow = TimeUtils.get90DaysDate(new Date());
        Date fromDate = TimeUtils.get90DaysDate(toDate);
        return fromDate.equals(last90DaysFromNow) || fromDate.after(last90DaysFromNow);
    }
}
