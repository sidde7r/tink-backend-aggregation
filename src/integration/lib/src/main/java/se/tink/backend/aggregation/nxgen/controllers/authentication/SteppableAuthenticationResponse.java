package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;

public final class SteppableAuthenticationResponse implements AuthenticationSteppable {

    private final Class<? extends AuthenticationStep> klass;
    private final AuthenticationResponse response;

    private SteppableAuthenticationResponse(
            final Class<? extends AuthenticationStep> klass,
            @Nonnull final AuthenticationResponse response) {
        this.klass = klass;
        this.response = Preconditions.checkNotNull(response);
    }

    public static SteppableAuthenticationResponse intermediateResponse(
            @Nonnull final Class<? extends AuthenticationStep> klass,
            @Nonnull final AuthenticationResponse response) {

        return new SteppableAuthenticationResponse(Preconditions.checkNotNull(klass), response);
    }

    public static SteppableAuthenticationResponse finalResponse(
            @Nonnull final AuthenticationResponse response) {
        return new SteppableAuthenticationResponse(null, response);
    }

    @Override
    public Optional<Class<? extends AuthenticationStep>> getStep() {
        return Optional.ofNullable(klass);
    }

    public AuthenticationResponse getPayload() {
        return response;
    }
}
