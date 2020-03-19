package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class OAuth2TokenResponseStandardParser implements OAuth2TokenResponseParser {

    private static final String ATTRIBUTE_ACCESS_TOKEN = "access_token";
    private static final String ATTRIBUTE_TOKEN_TYPE = "token_type";
    private static final String ATTRIBUTE_EXPIRES_IN = "expires_in";
    private static final String ATTRIBUTE_REFRESH_TOKEN = "refresh_token";
    private static final String ATTRIBUTE_SCOPE = "refresh_token";

    private Long defaultTokenLifetime;
    private Set<String> clientSpecificResponseProperties = new HashSet<>();

    public OAuth2TokenResponseStandardParser(final Long defaultTokenLifetime) {
        this(defaultTokenLifetime, new HashSet<>());
    }

    public OAuth2TokenResponseStandardParser(final Set<String> clientSpecificResponseProperties) {
        this(null, clientSpecificResponseProperties);
    }

    public OAuth2TokenResponseStandardParser(
            final Long defaultTokenLifetime, final Set<String> clientSpecificResponseProperties) {
        this.defaultTokenLifetime = defaultTokenLifetime;
        this.clientSpecificResponseProperties.addAll(clientSpecificResponseProperties);
    }

    @Override
    public OAuth2Token parse(final String accessTokenRawResponse) {
        try {
            JSONObject jsonObjectResponse = new JSONObject(accessTokenRawResponse);
            final OAuth2Token token = new OAuth2Token();
            parseStandardProperties(jsonObjectResponse, token);
            parseClientSpecificProperties(jsonObjectResponse, token);
            ensureThatExpirationTimeISet(token);
            return token;
        } catch (JSONException e) {
            throw new OAuth2AuthorizationException(
                    OAuth2AuthorizationErrorType.UNSUPPORTED_RESPONSE_TYPE,
                    "Response has incorrect format");
        }
    }

    private void ensureThatExpirationTimeISet(final OAuth2Token token) {
        if (token.getExpiresIn() == null) {
            token.setExpiresIn(
                    Optional.ofNullable(defaultTokenLifetime)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Access token lifetime can't be find neither in authorization server response, neither in authorization params provider")));
        }
    }

    private void parseStandardProperties(
            final JSONObject jsonObjectResponse, final OAuth2Token token) throws JSONException {
        token.setAccessToken(jsonObjectResponse.getString(ATTRIBUTE_ACCESS_TOKEN));
        token.setTokenType(jsonObjectResponse.getString(ATTRIBUTE_TOKEN_TYPE));
        token.setExpiresIn(getJsonLongValue(jsonObjectResponse, ATTRIBUTE_EXPIRES_IN));
        token.setRefreshToken(getJsonStringValue(jsonObjectResponse, ATTRIBUTE_REFRESH_TOKEN));
        token.setScope(getJsonStringValue(jsonObjectResponse, ATTRIBUTE_SCOPE));
    }

    private void parseClientSpecificProperties(
            final JSONObject jsonObjectResponse, final OAuth2Token token) throws JSONException {
        for (String property : clientSpecificResponseProperties) {
            token.addClientSpecificProperties(
                    property, getJsonStringValue(jsonObjectResponse, property));
        }
    }

    private Long getJsonLongValue(final JSONObject jsonObjectResponse, final String property)
            throws JSONException {
        return jsonObjectResponse.has(property) ? jsonObjectResponse.getLong(property) : null;
    }

    private String getJsonStringValue(final JSONObject jsonObjectResponse, final String property)
            throws JSONException {
        return jsonObjectResponse.has(property) ? jsonObjectResponse.getString(property) : null;
    }
}
