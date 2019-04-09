package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.onetimecode;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

/**
 * This controller is meant to be used for providers that use a one time activation code for the
 * activation step, and thereafter auto authentication. The controller extends the
 * PasswordAuthenticationController since the functionality is the same. It also implements the
 * MultiFactorAuthenticator since the AutoAuthenticationController takes a MultiFactorAuthenticator
 * and an AutoAuthenticator.
 */
public class OneTimeActivationCodeAuthenticationController extends PasswordAuthenticationController
        implements MultiFactorAuthenticator {

    public OneTimeActivationCodeAuthenticationController(PasswordAuthenticator authenticator) {
        super(authenticator);
    }
}
