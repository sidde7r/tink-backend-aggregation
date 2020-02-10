package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankCreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BuddybankAuthenticationController implements Authenticator {
    private final BuddybankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalRequester supplementalRequester;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public BuddybankAuthenticationController(
            BuddybankAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalRequester supplementalRequester) {
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalRequester = supplementalRequester;
    }

    @Override
    public void authenticate(Credentials credentials) {
        BuddybankCreateConsentResponse consentStatus =
                authenticator.createConsentRequest(strongAuthenticationState.getState());
        displayVerificationCode(credentials, consentStatus.getPsuMessage());
        awaitConsentResponse();
    }

    private void displayVerificationCode(Credentials credentials, String psuMessage) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Status")
                        .value("Waiting for bank consent")
                        .name("name")
                        .helpText(
                                String.format(
                                        "Message from the bank: %s Follow the instructions and continue by clicking update.",
                                        psuMessage))
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }

    private void awaitConsentResponse() {
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
