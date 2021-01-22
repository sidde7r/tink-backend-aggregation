package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderValues;
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
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ArgentaApiClient {

    private final TinkHttpClient client;
    private final ArgentaConfiguration configuration;
    private final String redirectUrl;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;

    public ArgentaApiClient(
            TinkHttpClient client,
            AgentConfiguration<ArgentaConfiguration> agentConfiguration,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            SignatureHeaderProvider signatureHeaderProvider) {
        Preconditions.checkNotNull(agentConfiguration);

        this.client = client;
        this.configuration =
                Preconditions.checkNotNull(agentConfiguration.getProviderSpecificConfiguration());
        this.redirectUrl = Preconditions.checkNotNull(agentConfiguration.getRedirectUrl());
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.signatureHeaderProvider = signatureHeaderProvider;
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
                .header(
                        HeaderKeys.SIGNATURE,
                        signatureHeaderProvider.generateSignatureHeader(headers));
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
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
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
                        true,
                        new ConsentRequestAccessEntity(ibans),
                        4);

        return createRequest(Urls.CONSENT, SerializationUtils.serializeToString(consentRequest))
                .post(ConsentResponse.class, consentRequest);
    }

    public void selectAuthenticationMethod(
            String selectAuthenticationMethodUrl, String authenticationMethodId) {
        ScaSelectionRequest scaSelectionRequest = new ScaSelectionRequest(authenticationMethodId);
        createRequest(
                        new URL(Urls.BASE_BERLIN_GROUP + selectAuthenticationMethodUrl),
                        SerializationUtils.serializeToString(scaSelectionRequest))
                .type(HeaderValues.JSON_UTF_8)
                .put(String.class, scaSelectionRequest);
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenRequest tokenRequest =
                new TokenRequest(
                        code,
                        FormValues.AUTHORIZATION_CODE,
                        configuration.getClientId(),
                        redirectUrl,
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return createRequest(Urls.TOKEN, tokenRequest.toData())
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    public AccountResponse getAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(HeaderKeys.DATE, getFormattedDate())
                .removeAggregatorHeader()
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountResponse.class);
    }

    public TransactionsResponse getTransactions(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.DATE, getFormattedDate())
                .get(TransactionsResponse.class);
    }

    private String getFormattedDate() {
        return DateTimeFormatter.ofPattern(ArgentaConstants.Formats.HEADER_DATE_FORMAT)
                .format(ZonedDateTime.now(ZoneOffset.UTC));
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
}
