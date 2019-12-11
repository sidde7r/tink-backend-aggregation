package se.tink.sa.framework.facade;

import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.GetConsentStatusRequest;
import se.tink.sa.model.auth.GetConsentStatusResponse;

public interface AuthenticationFacade {
    AuthenticationResponse getConsent(AuthenticationRequest request);

    GetConsentStatusResponse getConsentStatus(GetConsentStatusRequest request);
}
