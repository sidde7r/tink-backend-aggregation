package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;

public interface Xs2aAuthenticationDataAccessor extends OAuth2TokenAccessor {

    String getConsent();
}
