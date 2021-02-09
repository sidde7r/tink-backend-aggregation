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
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankCreateConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class BuddybankAuthenticationController implements Authenticator {
    private final BuddybankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;
    private static final LocalizableKey DESCRIPTION = new LocalizableKey("description");
    private static final String FIELD_KEY = "name";
    private static final LocalizableKey VALUE = new LocalizableKey("Waiting for bank consent");
    private static final LocalizableParametrizedKey HELP_TEXT =
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
        BuddybankCreateConsentResponse consentStatus =
                authenticator.createConsentRequest(strongAuthenticationState.getState());
        displayVerificationCode(consentStatus.getPsuMessage());
        awaitConsentResponse();
    }

    private void displayVerificationCode(String psuMessage) {
        Field field =
                CommonFields.Information.build(
                        FIELD_KEY,
                        catalog.getString(DESCRIPTION),
                        catalog.getString(VALUE),
                        catalog.getString(HELP_TEXT, psuMessage));

        supplementalInformationController.askSupplementalInformationAsync(field);
    }

    private void awaitConsentResponse() {
        Retryer<ConsentDetailsResponse> consentDetailsRetryer = getConsentDetailsRetryer();
        try {
            consentDetailsRetryer.call(authenticator::getConsentDetails);
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
}
