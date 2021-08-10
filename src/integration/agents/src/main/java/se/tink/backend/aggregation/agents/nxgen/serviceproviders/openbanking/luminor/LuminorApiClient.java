package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor;

import com.google.common.base.Strings;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.Language;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.configuration.LuminorConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class LuminorApiClient {

    private final String redirectUrl;
    private final String locale;
    private final String providerMarket;
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final LuminorConfiguration configuration;
    private final LuminorUserIpInformation userIpInformation;

    public LuminorApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String locale,
            String providerMarket,
            LuminorUserIpInformation userIpInformation,
            AgentConfiguration<LuminorConfiguration> configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration.getProviderSpecificConfiguration();
        this.redirectUrl = configuration.getRedirectUrl();
        this.locale = locale;
        this.providerMarket = providerMarket;
        this.userIpInformation = userIpInformation;
    }

    public URL getAuthorizeUrl(String state) {
        return createRequest(LuminorConstants.Urls.AUTH)
                .queryParam(LuminorConstants.QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(LuminorConstants.QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(LuminorConstants.QueryKeys.REALM, QueryValues.REALM)
                .queryParam(LuminorConstants.QueryKeys.STATE, state)
                .queryParam(QueryKeys.BANK_COUNTRY, providerMarket)
                .queryParam(QueryKeys.LOCALE, getLanguage(locale))
                .queryParam(QueryKeys.INFO_LOGO_LABEL, QueryValues.TINK)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .getUrl();
    }

    public String getLanguage(String language) {
        boolean valid = language.matches("[A-Za-z0-9_]{5,}");
        if (!valid || Strings.isNullOrEmpty(language)) {
            return Language.ENGLISH;
        } else {
            return language.substring(0, language.length() - 3);
        }
    }

    public RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    public OAuth2Token createToken(final String code) {
        TokenResponse response =
                client.request(LuminorConstants.Urls.TOKEN)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                        .queryParam(QueryKeys.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                        .queryParam(QueryKeys.CODE, code)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                        .queryParam(QueryKeys.REALM, QueryValues.REALM)
                        .post(TokenResponse.class);

        return response.toTinkToken();
    }

    public ConsentResponse createConsentResource(
            List<String> ibans, String strongAuthenticationState) {
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new ConsentRequestAccessEntity(ibans),
                        FormValues.FREQUENCY,
                        FormValues.RECURRING_INDICATOR,
                        FormValues.MAX_DATE,
                        FormValues.COMBINED_SERVICE_INDICATOR);

        return createRequest(Urls.CONSENT)
                .body(consentRequest, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.PSU_IP_ADDRESS, userIpInformation.getUserIp())
                .header(Psd2Headers.Keys.AUTHORIZATION, getBearerToken())
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.TPP_REDIRECT, HeaderValues.TRUE)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        redirectUrl + "?code=ok&state=" + strongAuthenticationState)
                .header(
                        HeaderKeys.TPP_REDIRECT_NOK_URI,
                        redirectUrl + "?code=nok&state=" + strongAuthenticationState)
                .post(ConsentResponse.class);
    }

    protected RequestBuilder createRequestInSessionNoPsuIp(URL url) {
        return createRequest(url)
                .header(Psd2Headers.Keys.AUTHORIZATION, getBearerToken())
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId());
    }

    protected RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(HeaderKeys.PSU_IP_ADDRESS, userIpInformation.getUserIp())
                .header(Psd2Headers.Keys.AUTHORIZATION, getBearerToken())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId());
    }

    private String getBearerToken() {
        OAuth2Token token = getTokenFromStorage();
        return token.toAuthorizeHeader();
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> {
                            log.info(ErrorMessages.MISSING_TOKEN);
                            return new IllegalStateException(
                                    SessionError.SESSION_EXPIRED.exception());
                        });
    }

    public boolean isConsentValid(String consentId) {
        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }
        ConsentStatusResponse response = getConsentStatus(consentId);
        return response.getConsentStatus().equals(QueryValues.VALID);
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequestInSession(
                        Urls.CONSENT_STATUS.parameter(PathParameterKeys.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public ConsentResponse getConsentDetails(String consentId) {
        return createRequestInSession(
                        Urls.CONSENT_DETAILS.parameter(PathParameterKeys.CONSENT_ID, consentId))
                .get(ConsentResponse.class);
    }

    public OAuth2Token refreshToken(String refreshToken) {
        TokenResponse response =
                client.request(LuminorConstants.Urls.TOKEN)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                        .queryParam(QueryKeys.GRANT_TYPE, QueryValues.REFRESH_TOKEN)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                        .queryParam(QueryKeys.REALM, QueryValues.REALM)
                        .queryParam(QueryKeys.REFRESH_TOKEN, refreshToken)
                        .post(TokenResponse.class);

        return response.toTinkToken();
    }

    public AccountsResponse getAccountList() {
        return createRequestInSessionNoPsuIp(LuminorConstants.Urls.ACCOUNT_LIST)
                .get(AccountsResponse.class);
    }

    public AccountsResponse getAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(
                        Psd2Headers.Keys.CONSENT_ID,
                        persistentStorage.get(Psd2Headers.Keys.CONSENT_ID))
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(AccountsResponse.class);
    }

    public AccountDetailsResponse getAccountDetails(String accountId) {
        return createRequestInSession(
                        Urls.ACCOUNT_DETAILS.parameter(PathParameterKeys.ACCOUNT_ID, accountId))
                .header(
                        Psd2Headers.Keys.CONSENT_ID,
                        persistentStorage.get(Psd2Headers.Keys.CONSENT_ID))
                .get(AccountDetailsResponse.class);
    }

    public BalancesResponse getAccountBalance(String accountId) {
        return createRequestInSession(
                        Urls.ACCOUNT_BALANCES.parameter(PathParameterKeys.ACCOUNT_ID, accountId))
                .header(
                        Psd2Headers.Keys.CONSENT_ID,
                        persistentStorage.get(Psd2Headers.Keys.CONSENT_ID))
                .get(BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(String accountId, String dateFrom, String dateTo) {
        return createRequestInSession(
                        Urls.ACCOUNT_TRANSACTIONS.parameter(
                                PathParameterKeys.ACCOUNT_ID, accountId))
                .header(
                        Psd2Headers.Keys.CONSENT_ID,
                        persistentStorage.get(Psd2Headers.Keys.CONSENT_ID))
                .queryParam(QueryKeys.DATE_FROM, dateFrom)
                .queryParam(QueryKeys.DATE_TO, dateTo)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .get(TransactionsResponse.class);
    }
}
