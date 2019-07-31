package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.CryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils.TimeUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BelfiusApiClient {

    private final TinkHttpClient client;
    private BelfiusConfiguration configuration;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public BelfiusApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    protected void setConfiguration(BelfiusConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {

        return client.request(url)
                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                .header(HeaderKeys.ACCEPT, HeaderValues.ACCEPT)
                .header(HeaderKeys.X_TINK_DEBUG, HeaderValues.FORCE);
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID());
    }

    public ConsentResponse[] getConsent(URL url) {

        String code = CryptoUtils.getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE, code);

        return createRequestInSession(url)
                .queryParam(QueryKeys.IBAN, credentials.getField(CredentialKeys.IBAN))
                .header(HeaderKeys.CODE_CHALLENGE, CryptoUtils.getCodeChallenge(code))
                .header(HeaderKeys.CODE_CHALLENGE_METHOD, HeaderValues.CODE_CHALLENGE_TYPE)
                .get(ConsentResponse[].class);
    }

    public TokenResponse postToken(URL url, String tokenEntity) {
        return createRequest(url)
                .addBasicAuth(configuration.getClientId(), configuration.getClientSecret())
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID())
                .body(tokenEntity, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    public FetchAccountResponse fetchAccountById() {

        final OAuth2Token oAuth2Token =
                persistentStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));

        return createRequestInSession(
                        new URL(
                                configuration.getBaseUrl()
                                        + Urls.FETCH_ACCOUNT_PATH
                                        + persistentStorage.get(StorageKeys.LOGICAL_ID)))
                .addBearerToken(oAuth2Token)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(Date fromDate, Date toDate) {
        URL url =
                new URL(configuration.getBaseUrl() + Urls.FETCH_TRANSACTIONS_PATH)
                        .parameter(
                                StorageKeys.LOGICAL_ID,
                                persistentStorage.get(StorageKeys.LOGICAL_ID));
        final OAuth2Token oAuth2Token =
                persistentStorage
                        .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));

        HttpResponse httpResponse =
                createRequestInSession(url)
                        .addBearerToken(oAuth2Token)
                        .queryParam(
                                QueryKeys.FROM_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(
                                        TimeUtils.get90DaysDate(toDate)))
                        .queryParam(
                                QueryKeys.TO_DATE,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                        .get(HttpResponse.class);

        try {
            return OBJECT_MAPPER.readValue(
                    httpResponse.getBodyInputStream(), FetchTransactionsResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(ErrorMessages.IO_FETCH_TRANSACTION, e);
        }
    }
}
