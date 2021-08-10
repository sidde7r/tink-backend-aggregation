package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableStore;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TokenDetector implements VariableDetector {

    @Override
    public boolean detectVariableFromInsertion(
            VariableStore variableStore, String storageKey, Object storageValue) {

        if (!(storageValue instanceof OAuth2TokenBase)) {
            return false;
        }

        populateTokenRelatedVariables(variableStore, (OAuth2TokenBase) storageValue);
        return true;
    }

    @Override
    public boolean detectVariableFromStorage(
            VariableStore variableStore, String storageKey, String storageValue) {
        Oauth2TokenBaseImpl oauthToken =
                SerializationUtils.deserializeFromString(storageValue, Oauth2TokenBaseImpl.class);

        if (isNotAToken(oauthToken)) {
            return false;
        }

        populateTokenRelatedVariables(variableStore, oauthToken);
        return true;
    }

    private boolean isNotAToken(Oauth2TokenBaseImpl oauthToken) {
        return oauthToken == null
                || oauthToken.getAccessToken() == null
                || oauthToken.getTokenType() == null;
    }

    private void populateTokenRelatedVariables(VariableStore variableStore, OAuth2TokenBase token) {
        variableStore.addVariable(
                VariableKey.AUTHORIZATION,
                toAuthorizeHeader(token.getTokenType(), token.getAccessToken()));
        variableStore.addVariable(VariableKey.ACCESS_TOKEN, token.getAccessToken());
        token.getRefreshToken()
                .ifPresent(
                        refreshToken ->
                                variableStore.addVariable(VariableKey.REFRESH_TOKEN, refreshToken));
    }

    private String toAuthorizeHeader(String tokenType, String accessToken) {
        // E.g. `Bearer XYZ`
        return String.format(
                "%s %s",
                tokenType.substring(0, 1).toUpperCase() + tokenType.substring(1).toLowerCase(),
                accessToken);
    }

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE)
    private static class Oauth2TokenBaseImpl extends OAuth2TokenBase {

        @Override
        public boolean isTokenTypeValid() {
            return false;
        }
    }
}
