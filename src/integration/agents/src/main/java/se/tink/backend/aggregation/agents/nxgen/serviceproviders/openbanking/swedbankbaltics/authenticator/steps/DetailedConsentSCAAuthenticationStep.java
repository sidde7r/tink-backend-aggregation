package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import com.google.common.util.concurrent.Uninterruptibles;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class DetailedConsentSCAAuthenticationStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;
    private final PersistentStorage persistentStorage;

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        Optional<ConsentResponse> consentResponse = stepDataStorage.getConsentResponse();

        if (consentResponse.isPresent()) {
            String url = consentResponse.get().getLinks().getHrefEntity().getHref();
            AuthenticationResponse authResponse = apiClient.authorizeConsent(url);

            Uninterruptibles.sleepUninterruptibly(
                    TimeValues.SCA_STATUS_POLL_DELAY, TimeUnit.MILLISECONDS);

            for (int i = 0; i < TimeValues.SCA_STATUS_POLL_MAX_ATTEMPTS; i++) {
                String status = apiClient.getScaStatus(authResponse.getCollectAuthUri());

                switch (status.toLowerCase()) {
                    case AuthStatus.RECEIVED:
                    case AuthStatus.STARTED:
                        logger.warn("Waiting for authentication");
                        break;
                    case AuthStatus.FINALIZED:
                        persistentStorage.put(
                                SwedbankConstants.StorageKeys.CONSENT,
                                consentResponse.get().getConsentId());
                        return AuthenticationStepResponse.authenticationSucceeded();
                    case AuthStatus.FAILED:
                        throw AuthorizationError.UNAUTHORIZED.exception();
                    default:
                        logger.warn("Unknown status {}", status);
                        throw AuthorizationError.UNAUTHORIZED.exception();
                }

                Uninterruptibles.sleepUninterruptibly(
                        TimeValues.SCA_STATUS_POLL_FREQUENCY, TimeUnit.MILLISECONDS);
            }

            logger.warn("Timeout");
        }

        throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
    }

    @Override
    public String getIdentifier() {
        return SwedbankBalticsConstants.DETAILED_CONSENT_SCA_AUTH_STEP;
    }
}
