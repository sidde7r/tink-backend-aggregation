package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
// Used when token expires when fetching data
public class PolishApiRefreshTokenFilter extends AbstractRetryFilter {

    private final PolishApiAuthorizationClient authorizationApiClient;
    private final PolishApiPersistentStorage persistentStorage;

    public PolishApiRefreshTokenFilter(
            PolishApiAuthorizationClient authorizationApiClient,
            PolishApiPersistentStorage persistentStorage,
            int maxNumRetries,
            long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
        this.authorizationApiClient = authorizationApiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
            String reponse = response.getBody(String.class);
            if (PolishApiErrors.isScaRequiredMessage(reponse)) {
                return false;
            }
            log.warn("[Polish API] Refresh Token Filter - Faced 401 error");
            Optional<String> refreshToken = persistentStorage.getToken().getRefreshToken();
            if (refreshToken.isPresent()) {
                log.info("[Polish API] Refresh Token Filter - Token is valid trying to exchange");
                TokenResponse tokenResponse =
                        authorizationApiClient.exchangeRefreshToken(refreshToken.get());
                persistentStorage.persistToken(tokenResponse.toOauthToken());
                log.info("[Polish API] Refresh Token Filter - Token successfully exchanged");
                return true;
            }
            log.warn("[Polish API] Refresh Token Filter - No token in persistent storage");
        }
        return false;
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return false;
    }
}
