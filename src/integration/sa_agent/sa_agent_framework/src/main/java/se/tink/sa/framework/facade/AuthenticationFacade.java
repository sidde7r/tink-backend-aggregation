package se.tink.sa.framework.facade;

import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;

public interface AuthenticationFacade {
    AuthenticationResponse getConsent(AuthenticationRequest request);
}
