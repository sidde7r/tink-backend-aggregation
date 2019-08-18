package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveTypedAuthenticator;

public class ThirdPartyAppAuthenticationProgressiveController<T>
        implements ProgressiveTypedAuthenticator {

    private final ThirdPartyAppAuthenticator<T> authenticator;
    private final int maxPollAttempts;

    private static final int DEFAULT_MAX_ATTEMPTS = 90;

    public ThirdPartyAppAuthenticationProgressiveController(
            ThirdPartyAppAuthenticator<T> authenticator) {
        this(authenticator, DEFAULT_MAX_ATTEMPTS);
    }

    public ThirdPartyAppAuthenticationProgressiveController(
            ThirdPartyAppAuthenticator<T> authenticator, int maxPollAttempts) {
        Preconditions.checkArgument(maxPollAttempts > 0);
        this.authenticator = authenticator;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    @Override
    public Iterable<? extends AuthenticationStep> authenticationSteps(
            final Credentials credentials) {
        return Arrays.asList(
                new OpenThirdPartyAppStep<>(authenticator),
                new RedirectStep<>(authenticator, maxPollAttempts));
    }
}
