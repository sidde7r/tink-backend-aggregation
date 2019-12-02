package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;

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

    private SteppableAuthenticationRequest() {
        stepIdentifier = null;
        payload = AuthenticationRequest.empty();
    }

    public static SteppableAuthenticationRequest initialRequest() {
        return new SteppableAuthenticationRequest();
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
