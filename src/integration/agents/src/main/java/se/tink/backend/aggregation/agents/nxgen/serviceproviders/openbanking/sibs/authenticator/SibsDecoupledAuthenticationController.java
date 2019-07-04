package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import java.util.concurrent.ExecutionException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class SibsDecoupledAuthenticationController implements Authenticator {

    private final SibsAuthenticator authenticator;
    private final String state;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public SibsDecoupledAuthenticationController(SibsAuthenticator authenticator) {
        this.authenticator = authenticator;
        this.state = SibsUtils.getRequestId();
    }

    @Override
    public void authenticate(Credentials credentials) {

        authenticator.initializeConsent(
                state,
                SibsConstants.HeaderValues.CLIENTE_PARTICULAR,
                credentials.getField(CredentialKeys.PSU_ID));

        Retryer<ConsentStatus> consentStatusRetryer =
                SibsUtils.getConsentStatusRetryer(SLEEP_TIME, RETRY_ATTEMPTS);

        try {
            ConsentStatus status = consentStatusRetryer.call(authenticator::getConsentStatus);

            if (!status.isAcceptedStatus()) {
                throw new IllegalStateException(
                        String.format(
                                "Authorization failed, consents status is not accepted. Current: %s Expected: %s!",
                                status.name(), ConsentStatus.ACTC.name()));
            }
        } catch (RetryException e) {
            throw new IllegalStateException(
                    String.format("Not able to fetch consents after %s attempts!", RETRY_ATTEMPTS),
                    e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization API error!", e);
        }
    }
}
