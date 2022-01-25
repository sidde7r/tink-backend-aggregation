package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk;

import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public interface DkSSAuthenticatorProvider {

    /**
     * It is important to call this method only right when we need the authenticator. Screen
     * scraping authenticators usually have to initialize WebDriver which is time and resource
     * consuming - we should initialize them only when we are indeed going to use them.
     */
    Authenticator initializeAuthenticator();
}
