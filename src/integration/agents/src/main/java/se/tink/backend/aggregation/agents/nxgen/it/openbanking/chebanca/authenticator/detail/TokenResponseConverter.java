package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.detail;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class TokenResponseConverter {

    public static OAuth2Token toOAuthToken(TokenResponse response) {
        return Optional.of(response)
                .map(TokenResponse::getData)
                .map(
                        tokenData ->
                                OAuth2Token.create(
                                        tokenData.getTokenType(),
                                        tokenData.getAccessToken(),
                                        tokenData.getRefreshToken(),
                                        tokenData.getExpiresIn(),
                                        tokenData.getRtExpiresIn()))
                .orElseThrow(
                        () ->
                                new RequiredDataMissingException(
                                        "Data needed to create OAuthToken is missing"));
    }
}
