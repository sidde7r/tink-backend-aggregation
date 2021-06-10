package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor
public class KnabApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String psuIpAddress;

    private KnabConfiguration configuration;
    private String redirectUrl;

    public void setConfiguration(AgentConfiguration<KnabConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.DATE, getDate())
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
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

    public URL buildAuthorizeUrl(String state, String scope) {
        return client.request(KnabConstants.Urls.AUTHORIZE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, scope)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .getUrl();
    }

    public boolean isConsentValid(String consentId, OAuth2Token oAuth2Token) {
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        return createRequest(Urls.CONSENT_STATUS.parameter(UrlParameters.CONSENT_ID, consentId))
                .addBearerToken(oAuth2Token)
                .get(ConsentResponse.class)
                .getConsentStatus()
                .equalsIgnoreCase(BodyValues.VALID);
    }

    public ConsentResponse getConsent(OAuth2Token token) {

        List<IbanEntity> ibans =
                Stream.of(credentials.getField(CredentialKeys.IBANS).split(","))
                        .map(String::trim)
                        .map(IbanEntity::new)
                        .collect(Collectors.toList());

        ConsentRequest consentRequest =
                new ConsentRequest(
                        LocalDate.now().plusDays(90).toString(),
                        new ConsentRequestAccessEntity(ibans),
                        4,
                        true,
                        false);

        return createRequest(Urls.CONSENT)
                .addBearerToken(token)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .post(ConsentResponse.class, consentRequest);
    }

    public static String getDate() {
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat(Formats.CONSENT_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public TokenResponse exchangeAuthorizationCode(String code, String state) {

        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .grantType(FormValues.AUTHORIZATION_CODE)
                        .code(code)
                        .clientId(configuration.getClientId())
                        .clientSecret(configuration.getClientSecret())
                        .state(state)
                        .redirectUri(redirectUrl)
                        .build();

        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequest.toTokenData());
    }

    public OAuth2Token refreshToken(String refreshToken) {

        TokenRequest tokenRequest =
                TokenRequest.builder()
                        .grantType(FormValues.REFRESH_TOKEN)
                        .clientId(configuration.getClientId())
                        .clientSecret(configuration.getClientSecret())
                        .refreshToken(refreshToken)
                        .build();

        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenRequest.toRefreshTokenData())
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalance(String accountId) {
        return createRequestInSession(Urls.BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId))
                .get(BalancesResponse.class);
    }

    public PaginatorResponse fetchTransactions(String accountId, Date fromDate, Date toDate) {
        return createRequestInSession(
                        Urls.TRANSACTIONS.parameter(PathVariables.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }
}
