package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.PollStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.SupplementalInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

@RequiredArgsConstructor
public class Xs2aDevelopersDecoupledAuthenticationController {

    private final Xs2aDevelopersAuthenticator authenticator;
    private final SupplementalInformationController controller;
    private final SupplementalInformationFormer supplementalInformationFormer;

    private static final int MAX_POLL_ATTEMPTS = 40;

    public void authenticate() throws AuthenticationException, AuthorizationException {
        displayMessageAndWait();
        pollForConsentStatus();
        authenticator.storeConsentDetails();
    }

    public void autoAuthenticate()
            throws SessionException, LoginException, BankServiceException, AuthorizationException {
        if (!authenticator.getConsentStatus().isValid()) {
            authenticator.clearPersistentStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void pollForConsentStatus() throws ThirdPartyAppException {
        for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
            ConsentStatusResponse response = authenticator.getConsentStatus();
            switch (response.getConsentStatus()) {
                case PollStatus.VALID:
                    return;
                case PollStatus.FAILED:
                    throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
                default:
                    break;
            }
            Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
        }
        throw ThirdPartyAppError.TIMED_OUT.exception();
    }

    private void displayMessageAndWait() {
        Field field =
                supplementalInformationFormer.getField(SupplementalInfo.CONSENT_CONFIRMATION_FIELD);
        try {
            controller.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
