package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleConstants.XMLtags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.utils.CreditAgricoleUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth1Token;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class CreditAgricoleApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private CreditAgricoleConfiguration configuration;

    public CreditAgricoleApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        // Credit Agricole seems to not like our signature and returns 401 Unauthorized Proxy
        this.client.disableSignatureRequestHeader();
        this.sessionStorage = sessionStorage;
    }

    public OAuth1Token getRequestToken(String state) {
        String callbackUrl = getCallbackUrlWithState(state);
        String consumerKey = configuration.getClientId();

        List<NameValuePair> params = OAuthUtils.getRequestTokenParams(callbackUrl, consumerKey);

        String requestTokenUrl = getRequestTokenUrl();
        String authorizationHeader = getOauthAuthorizationHeader(requestTokenUrl, params);
        String response = oauthSignedRequest(requestTokenUrl, authorizationHeader);

        Map<String, String> responsePairs = OAuthUtils.parseFormResponse(response);
        return createToken(StringUtils.EMPTY, responsePairs);
    }

    public OAuth1Token getAccessToken(String oauthToken, String oauthVerifier)
            throws SessionException {
        String accessTokenUrl = getAccessTokenUrl();
        String consumerKey = configuration.getClientId();

        OAuth1Token temporaryToken = fetchTokenFromSession();

        List<NameValuePair> params =
                OAuthUtils.getAccessTokenParams(consumerKey, oauthToken, oauthVerifier);
        String authorizationHeader =
                getOauthAuthorizationHeader(
                        accessTokenUrl,
                        params,
                        temporaryToken.getOauthTokenSecret(),
                        HttpMethod.POST.name());
        String response = oauthSignedRequest(accessTokenUrl, authorizationHeader);

        Map<String, String> responsePairs = OAuthUtils.parseFormResponse(response);
        OAuth1Token oAuth1Token = createToken(oauthVerifier, responsePairs);
        setTokenToSession(oAuth1Token);
        return oAuth1Token;
    }

    public void setConfiguration(CreditAgricoleConfiguration configuration) {
        this.configuration = configuration;
    }

    public CreditAgricoleConfiguration getConfiguration() {
        return configuration;
    }

    public URL getAuthorizeUrl(String token) {
        return client.request(
                        new URL(
                                configuration.getBaseUrl()
                                        + CreditAgricoleConstants.Urls.AUTHENTICATION))
                .queryParam(CreditAgricoleConstants.QueryKeys.OAUTH_TOKEN, token)
                .getUrl();
    }

    public void setTokenToSession(OAuth1Token token) {
        sessionStorage.put(CreditAgricoleConstants.StorageKeys.TEMPORARY_TOKEN, token);
    }

    private String oauthSignedRequest(String url, String oauthSignedHeader) {
        return client.request(new URL(url))
                .accept(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, oauthSignedHeader)
                .post(String.class);
    }

    private String getRequestTokenUrl() {
        return configuration.getBaseUrl() + CreditAgricoleConstants.Urls.GET_REQUEST_TOKEN;
    }

    private OAuth1Token createToken(String oauthVerifier, Map<String, String> responsePairs) {
        return new OAuth1Token(
                responsePairs.get(CreditAgricoleConstants.FormKeys.OAUTH_TOKEN),
                responsePairs.get(CreditAgricoleConstants.FormKeys.OAUTH_TOKEN_SECRET),
                responsePairs.get(CreditAgricoleConstants.FormKeys.OAUTH_CALLBACK_CONFIRMED),
                oauthVerifier);
    }

    private OAuth1Token fetchTokenFromSession() throws SessionException {
        return sessionStorage
                .get(CreditAgricoleConstants.StorageKeys.TEMPORARY_TOKEN, OAuth1Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private String getAccessTokenUrl() {
        return configuration.getBaseUrl() + CreditAgricoleConstants.Urls.GET_ACCESS_TOKEN;
    }

    private String getOauthAuthorizationHeader(String baseUrl, List<NameValuePair> params) {
        return getOauthAuthorizationHeader(
                baseUrl, params, StringUtils.EMPTY, HttpMethod.POST.name());
    }

    private String getOauthAuthorizationHeader(
            String url, List<NameValuePair> params, String oauthSecret, String httpRequestMethod) {
        String consumerSecret = configuration.getClientSecret();
        String signature =
                OAuthUtils.getSignature(
                        url, httpRequestMethod, params, consumerSecret, oauthSecret);
        params.add(new BasicNameValuePair(OAuth1Constants.QueryParams.OAUTH_SIGNATURE, signature));
        return OAuthUtils.getAuthorizationHeaderValue(params);
    }

    private String getCallbackUrlWithState(String state) {
        return UriBuilder.fromUri(configuration.getRedirectUrl())
                .queryParam(CreditAgricoleConstants.QueryKeys.TINK_STATE, state)
                .build()
                .toString();
    }

    public void getUserIdIntoSession() throws SessionException {
        OAuth1Token temporaryToken = fetchTokenFromSession();
        String consumerKey = configuration.getClientId();

        List<NameValuePair> params =
                CreditAgricoleUtils.getUserIdRequestParams(
                        temporaryToken.getOauthToken(), consumerKey);

        String requestUserIdUrl = getUserIdUrl();
        String authorizationHeader =
                getOauthAuthorizationHeader(
                        requestUserIdUrl,
                        params,
                        temporaryToken.getOauthTokenSecret(),
                        HttpMethod.GET.name());
        String userXML =
                client.request(new URL(requestUserIdUrl))
                        .accept(MediaType.APPLICATION_XML)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .get(String.class);

        String userId = CreditAgricoleUtils.getXMLResponse(XMLtags.ID, userXML);
        sessionStorage.put(StorageKeys.USER_ID, userId);
    }

    private String getUserIdUrl() {
        return configuration.getBaseUrl() + Urls.REST_BASE_PATH + QueryKeys.USER_ID;
    }
}
