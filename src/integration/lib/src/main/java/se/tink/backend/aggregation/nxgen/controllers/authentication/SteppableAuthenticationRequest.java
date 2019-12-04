package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;
import se.tink.backend.agents.rpc.Credentials;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class SteppableAuthenticationRequest implements AuthenticationSteppable {
    private final String stepIdentifier;
    private final AuthenticationRequest payload;

    private SteppableAuthenticationRequest(
            final String stepIdentifier, final AuthenticationRequest payload) {
        this.stepIdentifier = stepIdentifier;
        this.payload = payload;
    }

    private SteppableAuthenticationRequest(final Credentials credentials) {
        stepIdentifier = null;
        payload = new AuthenticationRequest(credentials);
    }

    public static SteppableAuthenticationRequest initialRequest(final Credentials credentials) {
        return new SteppableAuthenticationRequest(credentials);
    }

    public static SteppableAuthenticationRequest subsequentRequest(
            @Nonnull final String stepIdentifier, @Nonnull final AuthenticationRequest payload) {
        return new SteppableAuthenticationRequest(
                stepIdentifier, Preconditions.checkNotNull(payload));
    }

    @Override
    public Optional<String> getStepIdentifier() {
        return Optional.ofNullable(stepIdentifier);
    }

    public AuthenticationRequest getPayload() {
        return payload;
    }
}
