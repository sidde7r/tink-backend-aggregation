package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class BuddybankAuthenticationController implements Authenticator {
    private final BuddybankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public BuddybankAuthenticationController(
            BuddybankAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public void authenticate(Credentials credentials) {

        authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());

        Retryer<ConsentStatusResponse> consentStatusRetryer = getConsentStatusRetryer();

        try {
            consentStatusRetryer.call(authenticator::getConsentStatus);

        } catch (RetryException e) {
            throw new IllegalStateException("Authorization status error!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization api error!");
        }
    }

    private Retryer<ConsentStatusResponse> getConsentStatusRetryer() {
        return RetryerBuilder.<ConsentStatusResponse>newBuilder()
                .retryIfResult(status -> !Objects.isNull(status) && !status.isValidConsent())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }
}
