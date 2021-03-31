package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.FormKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.FormValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HeaderKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.QueryParamKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.QueryParamValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.Urls;

import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationParams;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class AuthenticationClient {

    private final BaseClient baseClient;
    private final NordeaNoStorage storage;

    public AuthenticationsResponse getNordeaSessionDetails(
            String codeChallenge, String state, String nonce) {
        AuthenticationParams request = new AuthenticationParams(codeChallenge, nonce, state);
        return baseClient
                .request(Urls.NORDEA_AUTHENTICATION_START)
                .header(HeaderKeys.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(HeaderKeys.PLATFORM_TYPE, HeaderValues.PLATFORM_TYPE)
                .header(HeaderKeys.DEVICE_EC, HeaderValues.DEVICE_EC)
                .header(HeaderKeys.APP_VERSION, HeaderValues.APP_VERSION)
                .type(MediaType.APPLICATION_JSON)
                .post(AuthenticationsResponse.class, request);
    }

    public String constructUrlForBankIdAuthentication(
            String codeChallenge,
            String state,
            String nonce,
            String integrationUrl,
            String sessionId) {
        URL url =
                URL.of(Urls.BANKID_AUTHENTICATION_INIT)
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
                        .queryParam(QueryParamKeys.STATE, state);
        return url.toString();
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

    public OauthTokenResponse refreshAccessToken(String refreshToken) {
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
