package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.session;

import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.token.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacMultiToken;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class AmexSessionHandler implements SessionHandler {

    private final AmexApiClient amexApiClient;
    private final HmacMultiTokenStorage hmacMultiTokenStorage;
    private final Provider provider;

    @Override
    public void logout() {}

    /*
     * This sessionHandler is a duct tape solution for Amex. We want to enforce the SE - agent to always authenticate,
     * even if we have a valid token or not. This is due to we cannot manually revoke a token if it gets lost, which can
     * happen if a user deletes his/her account, or for temporary users.
     * After each refresh we will revoke the token and clear it from storage.
     * */
    @Override
    public void keepAlive() throws SessionException {

        HmacToken token =
                getTokenFromStorage().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        try {
            if (token.hasAccessExpired()) {
                token =
                        token.getRefreshToken()
                                .flatMap(amexApiClient::refreshAccessToken)
                                .map(AmexSessionHandler::convertResponseToHmacToken)
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

                hmacMultiTokenStorage.storeToken(
                        new HmacMultiToken(Collections.singletonList(token)));
            }

            amexApiClient.fetchAccounts(token);

        } catch (HttpResponseException | IllegalStateException e) {
            hmacMultiTokenStorage.clearToken();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (provider.getMarket().equalsIgnoreCase("se") && amexApiClient.shouldLogout()) {
            amexApiClient.revokeAccessToken();
        }
    }

    private Optional<HmacToken> getTokenFromStorage() {
        return hmacMultiTokenStorage
                .getToken()
                .flatMap(token -> token.getTokens().stream().findFirst());
    }

    private static HmacToken convertResponseToHmacToken(TokenResponseDto response) {
        return new HmacToken(
                response.getTokenType(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getMacKey(),
                response.getExpiresIn());
    }
}
