package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.FormKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.FormValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HeaderKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.QueryParamKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.QueryParamValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.UriParams;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.Urls;

import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsPatchRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsPatchResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.CodeExchangeReqResp;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class AuthenticationClient {

    private BaseClient baseClient;
    private final NordeaNoStorage storage;

    // Not sure if needed at all.
    public String initializeNordeaSession(
            String codeChallenge,
            String state,
            String nonce,
            String mobileNumber,
            String dateOfBirth) {
        RequestBuilder request =
                baseClient
                        .request(Urls.NORDEA_AUTH_BASE_URL)
                        .queryParam(QueryParamKeys.CLIENT_ID, QueryParamValues.CLIENT_ID)
                        .queryParam(QueryParamKeys.CODE_CHALLENGE, codeChallenge)
                        .queryParam(
                                QueryParamKeys.CODE_CHALLENGE_METHOD,
                                QueryParamValues.CODE_CHALLENGE_METHOD)
                        .queryParam(QueryParamKeys.STATE, state)
                        .queryParam(QueryParamKeys.NONCE, nonce)
                        .queryParam(QueryParamKeys.REDIRECT_URI, QueryParamValues.REDIRECT_URI)
                        .queryParam(QueryParamKeys.RESPONSE_TYPE, QueryParamValues.RESPONSE_TYPE)
                        .queryParam(QueryParamKeys.UI_LOCALES, QueryParamValues.UI_LOCALES)
                        .queryParam(QueryParamKeys.AV, QueryParamValues.AV)
                        .queryParam(QueryParamKeys.DM, QueryParamValues.DM)
                        .queryParam(QueryParamKeys.INSTALLED_APPS, QueryParamValues.INSTALLED_APPS)
                        .queryParam(QueryParamKeys.SCOPE, QueryParamValues.SCOPE)
                        .queryParam(
                                QueryParamKeys.LOGIN_HINT,
                                String.format(
                                        QueryParamValues.LOGIN_HINT_FORMAT,
                                        mobileNumber,
                                        dateOfBirth))
                        .queryParam(QueryParamKeys.APP_CHANNEL, QueryParamValues.APP_CHANNEL)
                        .queryParam(QueryParamKeys.ADOBE_MC, QueryParamValues.ADOBE_MC);
        String res = request.getUrl().toString();
        request.get(HttpResponse.class);
        return res;
    }

    public AuthenticationsResponse getNordeaSessionDetails(
            String codeChallenge, String state, String nonce, String referer) {
        AuthenticationParams request = new AuthenticationParams(codeChallenge, nonce, state);
        return baseClient
                .request(Urls.NORDEA_AUTHENTICATION_START)
                .header(HeaderKeys.REFERER, referer)
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.DEVICE_EC, HeaderValues.DEVICE_EC)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .type(MediaType.APPLICATION_JSON)
                .post(AuthenticationsResponse.class, request);
    }

    public HttpResponse initializeOidcSession(
            String codeChallenge,
            String state,
            String nonce,
            String referer,
            String integrationUrl,
            String sessionId,
            String mobileNumber,
            String dateOfBirth) {

        return baseClient
                .request(Urls.BANKID_AUTHENTICATION_INIT)
                .queryParam(QueryParamKeys.AV, QueryParamValues.AV)
                .queryParam(QueryParamKeys.CLIENT_ID, QueryParamValues.CLIENT_ID)
                .queryParam(QueryParamKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(
                        QueryParamKeys.CODE_CHALLENGE_METHOD,
                        QueryParamValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryParamKeys.DM, QueryParamValues.DM)
                .queryParam(QueryParamKeys.EC, QueryParamValues.EC)
                .queryParam(QueryParamKeys.INTEGRATION_URL, integrationUrl)
                .queryParam(QueryParamKeys.LANG, QueryParamValues.LANG)
                .queryParam(QueryParamKeys.METHOD_ID, QueryParamValues.METHOD_ID)
                .queryParam(QueryParamKeys.PLATFORM, QueryParamValues.PLATFORM)
                .queryParam(QueryParamKeys.REDIRECT_URI, QueryParamValues.REDIRECT_URI)
                .queryParam(QueryParamKeys.NONCE, nonce)
                .queryParam(QueryParamKeys.RESPONSE_TYPE, QueryParamValues.RESPONSE_TYPE)
                .queryParam(QueryParamKeys.SCOPE, QueryParamValues.SCOPE)
                .queryParam(QueryParamKeys.SESSION_ID, sessionId)
                .queryParam(QueryParamKeys.STATE, state)
                .queryParam(
                        QueryParamKeys.LOGIN_HINT,
                        String.format(
                                QueryParamValues.LOGIN_HINT_SHORT_FORMAT,
                                mobileNumber,
                                dateOfBirth))
                .header(HeaderKeys.REFERER, referer)
                .get(HttpResponse.class);
    }

    public HttpResponse checkOidcSession(String sessionId) throws LoginException {
        return baseClient
                .request(
                        new URL(Urls.BANKID_AUTHENTICATION_CHECK)
                                .parameter(UriParams.URI_SESSION_ID, sessionId))
                .get(HttpResponse.class);
    }

    public HttpResponse getBidCodeOfOidcSession(String oidcSessionId) {
        return baseClient
                .request(Urls.OIDC_BANKID_BASE_URL)
                .queryParam("sid", oidcSessionId)
                .get(HttpResponse.class);
    }

    public AuthenticationsPatchResponse authenticationsPatch(
            String bidCode, String sessionId, String referer) {
        AuthenticationsPatchRequest request = new AuthenticationsPatchRequest(bidCode);
        return baseClient
                .request(
                        new URL(Urls.NORDEA_AUTHENTICATION_PATCH)
                                .parameter(UriParams.URI_SESSION_ID, sessionId))
                .header(HeaderKeys.REFERER, referer)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.DEVICE_EC, HeaderValues.DEVICE_EC)
                .type(MediaType.APPLICATION_JSON)
                .patch(AuthenticationsPatchResponse.class, request);
    }

    public CodeExchangeReqResp codeExchange(String code, String referer) {
        CodeExchangeReqResp request = new CodeExchangeReqResp(code);
        return baseClient
                .request(Urls.NORDEA_AUTHORIZATION)
                .header(HeaderKeys.REFERER, referer)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.DEVICE_EC, HeaderValues.DEVICE_EC)
                .type(MediaType.APPLICATION_JSON)
                .post(CodeExchangeReqResp.class, request);
    }

    public OauthTokenResponse getOathToken(String code, String codeVerifier) {
        String body =
                Form.builder()
                        .put(FormKeys.AUTH_METHOD, FormValues.AUTH_METHOD)
                        .put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.COUNTRY, FormValues.COUNTRY)
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.REDIRECT_URI, FormValues.REDIRECT_URI)
                        .put(FormKeys.SCOPE, FormValues.SCOPE)
                        .build()
                        .serialize();
        return baseClient
                .commonNordeaPrivateRequest(Urls.EXCHANGE_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(OauthTokenResponse.class, body);
    }

    public OauthTokenResponse refreshAccessToken() {
        String refreshToken =
                storage.retrieveOauthToken()
                        .flatMap(OAuth2TokenBase::getRefreshToken)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        String body =
                Form.builder()
                        .put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID_REFRESH)
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return baseClient
                .baseAuthorizedRequest(Urls.EXCHANGE_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(OauthTokenResponse.class, body);
    }

    public void logout() {
        String accessToken =
                storage.retrieveOauthToken()
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception)
                        .getAccessToken();

        String body =
                Form.builder()
                        .put(FormKeys.TOKEN, accessToken)
                        .put(FormKeys.TOKEN_TYPE_HINT, FormValues.TOKEN_TYPE_HINT)
                        .build()
                        .serialize();

        baseClient
                .commonNordeaPrivateRequest(Urls.LOGOUT)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(body);
    }
}
