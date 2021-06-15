package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class BuddybankAuthenticationController implements Authenticator, AutoAuthenticator {
    private final BuddybankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private static final LocalizableParametrizedKey INSTRUCTIONS =
            new LocalizableParametrizedKey(
                    "Message from the bank: {0} Follow the instructions and continue by clicking update.");

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public BuddybankAuthenticationController(
            BuddybankAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
    }

    @Override
    public void authenticate(Credentials credentials) {
        BuddybankConsentResponse consentStatus =
                authenticator.createConsentRequest(strongAuthenticationState.getState());
        displayVerificationCode(consentStatus.getPsuMessage());
        ConsentDetailsResponse consentDetailsResponse = awaitConsentResponse();
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    private void displayVerificationCode(String psuMessage) {
        Field field = CommonFields.Instruction.build(catalog.getString(INSTRUCTIONS, psuMessage));

        supplementalInformationController.askSupplementalInformationAsync(field);
    }

    private ConsentDetailsResponse awaitConsentResponse() {
        Retryer<ConsentDetailsResponse> consentDetailsRetryer = getConsentDetailsRetryer();
        try {
            return consentDetailsRetryer.call(authenticator::getConsentDetails);
        } catch (RetryException e) {
            throw new IllegalStateException("Authorization status error!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization api error!");
        }
    }

    private Retryer<ConsentDetailsResponse> getConsentDetailsRetryer() {
        return RetryerBuilder.<ConsentDetailsResponse>newBuilder()
                .retryIfResult(status -> !Objects.isNull(status) && !status.isValid())
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        authenticator.getConsentId().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        Optional<ConsentDetailsResponse> maybeValidConsentDetails =
                authenticator.getConsentDetailsWithValidStatus();

        if (!maybeValidConsentDetails.isPresent()) {
            authenticator.clearConsent();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        authenticator.setCredentialsSessionExpiryDate(maybeValidConsentDetails.get());
    }
}
