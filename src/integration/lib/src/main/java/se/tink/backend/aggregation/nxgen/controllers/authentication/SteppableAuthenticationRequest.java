package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class SteppableAuthenticationRequest implements AuthenticationSteppable {
    private final Class<? extends AuthenticationStep> step;
    private final AuthenticationRequest payload;

    private SteppableAuthenticationRequest(
            final Class<? extends AuthenticationStep> step, final AuthenticationRequest payload) {
        this.step = step;
        this.payload = payload;
    }

    public static SteppableAuthenticationRequest initialRequest() {
        return new SteppableAuthenticationRequest(null, AuthenticationRequest.createEmpty());
    }

    public static SteppableAuthenticationRequest subsequentRequest(
            @Nonnull final Class<? extends AuthenticationStep> klass,
            @Nonnull final AuthenticationRequest payload) {
        return new SteppableAuthenticationRequest(
                Preconditions.checkNotNull(klass), Preconditions.checkNotNull(payload));
    }

    @Override
    public Optional<Class<? extends AuthenticationStep>> getStep() {
        return Optional.ofNullable(step);
    }

    public AuthenticationRequest getPayload() {
        return payload;
    }
}
