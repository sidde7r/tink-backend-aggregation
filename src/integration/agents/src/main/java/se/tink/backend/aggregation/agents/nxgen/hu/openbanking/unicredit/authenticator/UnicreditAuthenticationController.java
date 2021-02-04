package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

public class UnicreditAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final UnicreditAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    public UnicreditAuthenticationController(
            UnicreditAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {

        Retryer<ConsentDetailsResponse> consentDetailsRetryer = getConsentDetailsRetryer();

        try {
            consentDetailsRetryer.call(authenticator::getConsentDetails);
        } catch (RetryException e) {
            throw new IllegalStateException("Authorization status error!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Authorization api error!");
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private Retryer<ConsentDetailsResponse> getConsentDetailsRetryer() {
        return RetryerBuilder.<ConsentDetailsResponse>newBuilder()
                .retryIfResult(status -> !Objects.isNull(status) && !status.isValid())
                .retryIfException( // TODO remove retryIfException for prod (sandbox bug)
                        throwable ->
                                throwable instanceof HttpResponseException
                                        && ((HttpResponseException) throwable)
                                                        .getResponse()
                                                        .getStatus()
                                                == 400)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
