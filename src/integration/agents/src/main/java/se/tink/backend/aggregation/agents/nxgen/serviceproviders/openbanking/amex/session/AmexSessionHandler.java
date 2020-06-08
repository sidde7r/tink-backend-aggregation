package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.session;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class AmexSessionHandler implements SessionHandler {

    private final AmexApiClient amexApiClient;
    private final HmacMultiTokenStorage hmacMultiTokenStorage;
    private final Provider provider;

    @Override
    public void logout() {
        if (shouldLogOut()) {

            getTokenFromStorage()
                    .ifPresent(token -> amexApiClient.revokeAccessToken(token.getAccessToken()));

            hmacMultiTokenStorage.clearToken();
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            amexApiClient.fetchAccounts(
                    getTokenFromStorage().orElseThrow(SessionError.SESSION_EXPIRED::exception));

        } catch (HttpResponseException | IllegalStateException e) {

            hmacMultiTokenStorage.clearToken();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private Optional<HmacToken> getTokenFromStorage() {
        return hmacMultiTokenStorage
                .getToken()
                .flatMap(token -> token.getTokens().stream().findFirst());
    }

    // Temporary solution for Swedish Amex OB. We will always log out after a refresh, because of
    // the user can become blocked otherwise.
    private boolean shouldLogOut() {
        return provider.getMarket().equalsIgnoreCase("se");
    }
}
