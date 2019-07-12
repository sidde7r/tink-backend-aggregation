package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class SibsDecoupledAuthenticationController implements Authenticator {

    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private final SibsAuthenticator authenticator;
    private final String state;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public SibsDecoupledAuthenticationController(SibsAuthenticator authenticator) {
        this.authenticator = authenticator;
        this.state = generateRandomState();
    }

    @Override
    public void authenticate(Credentials credentials) {

        authenticator.initializeConsent(
                state,
                credentials.getField(CredentialKeys.PSU_ID_TYPE),
                credentials.getField(CredentialKeys.PSU_ID));

        Retryer<ConsentStatus> consentStatusRetryer = getConsentStatusRetryer();

        try {
            ConsentStatus status = consentStatusRetryer.call(authenticator::getConsentStatus);

            if (!status.isAcceptedStatus()) {
                throw new IllegalStateException("Authorization failed!");
            }
        } catch (RetryException e) {
            throw new IllegalStateException("Authorization status error!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization api error!");
        }
    }

    private Retryer<ConsentStatus> getConsentStatusRetryer() {
        return RetryerBuilder.<ConsentStatus>newBuilder()
                .retryIfResult(status -> status != null && status.isAwaitableStatus())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }
}
