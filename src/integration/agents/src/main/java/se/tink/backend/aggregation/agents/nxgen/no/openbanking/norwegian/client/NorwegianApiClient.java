package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.time.DateFormatUtils;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NorwegianApiClient {

    private static final String CODE_CHALLENGE_FIELD_PLACEHOLDER = "<YOUR_CODE_CHALLENGE>";
    private static final String WHITESPACE = " ";
    private static final String ESCAPED_WHITESPACE = "%20";

    private final TinkHttpClient client;
    private final NorwegianConfiguration norwegianConfiguration;
    private final String redirectUrl;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final String userIp;

    public NorwegianApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage,
            final AgentConfiguration<NorwegianConfiguration> agentConfiguration,
            final String userIp) {
        this.client = Objects.requireNonNull(client);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
        this.persistentStorage = Objects.requireNonNull(persistentStorage);
        Objects.requireNonNull(agentConfiguration);
        this.norwegianConfiguration =
                Objects.requireNonNull(agentConfiguration.getProviderSpecificConfiguration());
        this.redirectUrl = Objects.requireNonNull(agentConfiguration.getRedirectUrl());
        this.userIp = userIp;
    }

    private String getAuthorizationString() {
        return String.format(
                "%s:%s",
                norwegianConfiguration.getClientId(), norwegianConfiguration.getClientSecret());
    }

    public OAuth2Token exchangeAuthorizationToken(String code, String codeVerifier) {
        TokenRequest request =
                TokenRequest.builder()
                        .setClientId(norwegianConfiguration.getClientId())
                        .setClientSecret(norwegianConfiguration.getClientSecret())
                        .setCode(code)
                        .setGrantType(QueryValues.AUTHORIZATION_CODE)
                        .setRedirectUri(redirectUrl)
                        .setCodeVerifier(codeVerifier)
                        .build();

        return client.request(new URL(NorwegianConstants.URLs.TOKEN_URL))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class)
                .toOauthToken();
    }

    public OAuth2Token exchangeRefreshToken(RefreshRequest request) {
        return client.request(new URL(NorwegianConstants.URLs.TOKEN_URL))
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HeaderValues.BASIC
                                + Base64.getEncoder()
                                        .encodeToString(getAuthorizationString().getBytes()))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toOauthToken();
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        NorwegianConstants.ErrorMessages.MISSING_TOKEN));
    }

    public BalanceResponse getBalance(String resourceId) {
        return createRequestInSession(
                        new URL(
                                        NorwegianConstants.URLs.BASE_URL
                                                + NorwegianConstants.URLs.BALANCES_PATH)
                                .parameter(
                                        NorwegianConstants.IdTags.ACCOUNT_RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public TransactionsResponse getTransactions(
            String resourceId, Date dateFrom, Date dateTo, int page) {

        try {
            return getTransactionsBatch(resourceId, dateFrom, dateTo, page);
        } catch (HttpResponseException e) {
            // 204 means that there is no more transactions for given criteria, so empty response
            // should be returned
            if (e.getResponse().getStatus() == 204) {
                return new TransactionsResponse();
            }
            throw e;
        }
    }

    private TransactionsResponse getTransactionsBatch(
            String resourceId, Date dateFrom, Date dateTo, int page) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormatUtils.ISO_DATE_FORMAT.getPattern());

        return createRequestInSession(
                        new URL(
                                        NorwegianConstants.URLs.BASE_URL
                                                + NorwegianConstants.URLs.TRANSACTIONS_PATH)
                                .parameter(
                                        NorwegianConstants.IdTags.ACCOUNT_RESOURCE_ID, resourceId))
                .queryParam(QueryKeys.DATE_FROM, sdf.format(dateFrom))
                .queryParam(QueryKeys.DATE_TO, sdf.format(dateTo))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS_BOTH)
                .queryParam(QueryKeys.PAGE, String.valueOf(page))
                .get(TransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state, String codeChallenge) {
        ConsentRequest consentRequest =
                new ConsentRequest(
                        LocalDate.now()
                                .plus(90, ChronoUnit.DAYS)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        RequestBuilder request =
                client.request(
                                NorwegianConstants.URLs.BASE_URL
                                        + NorwegianConstants.URLs.CONSENT_PATH)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.TPP_CLIENT_ID, norwegianConfiguration.getClientId())
                        .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl);
        request = addMandatoryHeaders(request);
        ConsentResponse response = request.post(ConsentResponse.class, consentRequest);

        persistentStorage.put(StorageKeys.CONSENT_ID, response.getConsentId());
        persistentStorage.put(
                StorageKeys.CONSENT_CREATION_DATE,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return new URL(
                        response.getRedirectUrl()
                                .replaceAll(WHITESPACE, ESCAPED_WHITESPACE)
                                .replace(CODE_CHALLENGE_FIELD_PLACEHOLDER, codeChallenge))
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                .queryParam(QueryKeys.RESPONSE_MODE, QueryValues.QUERY);
    }

    public ConsentDetailsResponse getConsentDetails() {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        if (consentId == null) {
            return null;
        }

        return createRequestInSession(
                        new URL(
                                        NorwegianConstants.URLs.BASE_URL
                                                + NorwegianConstants.URLs.CONSENT_DETAILS_PATH)
                                .parameter(NorwegianConstants.IdTags.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    private RequestBuilder createRequestInSession(URL url) {
        RequestBuilder request =
                client.request(url)
                        .addBearerToken(getTokenFromSession())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID))
                        .accept(MediaType.APPLICATION_JSON);
        return addMandatoryHeaders(request);
    }

    RequestBuilder addMandatoryHeaders(RequestBuilder request) {
        String psuId = persistentStorage.get(StorageKeys.PSU_ID);
        if (psuId == null) {
            psuId = UUID.randomUUID().toString().toLowerCase();
            persistentStorage.put(StorageKeys.PSU_ID, psuId);
        }
        return request.header(HeaderKeys.DATE, formatDateHeader())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.PSU_IP_ADDRESS, userIp)
                .header(HeaderKeys.PSU_IP_PORT, HeaderValues.PSU_PORT)
                .header(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT)
                .header(HeaderKeys.REGION_ID, HeaderValues.REGION_ID)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID().toString().toLowerCase())
                .header(HeaderKeys.PSU_DEVICE_ID, HeaderValues.PSU_DEVICE_ID);
    }

    private String formatDateHeader() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(
                        new URL(
                                NorwegianConstants.URLs.BASE_URL
                                        + NorwegianConstants.URLs.ACCOUNTS_PATH))
                .get(AccountsResponse.class);
    }
}
