package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class SteppableAuthenticationRequest implements AuthenticationSteppable {
    private final Optional<String> stepIdentifier;
    private final AuthenticationRequest payload;
    private String persistentData;

    private SteppableAuthenticationRequest(
            final String stepIdentifier,
            final AuthenticationRequest payload,
            final String persistentData) {
        this.stepIdentifier = Optional.of(stepIdentifier);
        this.payload = payload;
        this.persistentData = persistentData;
    }

    private SteppableAuthenticationRequest() {
        stepIdentifier = Optional.empty();
        payload = AuthenticationRequest.empty();
    }

    public static SteppableAuthenticationRequest initialRequest() {
        return new SteppableAuthenticationRequest();
    }

    public static SteppableAuthenticationRequest subsequentRequest(
            @Nonnull final String stepIdentifier,
            @Nonnull final AuthenticationRequest payload,
            final String persistentData) {
        return new SteppableAuthenticationRequest(
                stepIdentifier, Preconditions.checkNotNull(payload), persistentData);
    }

    @Override
    public Optional<String> getStepIdentifier() {
        return stepIdentifier;
    }

    public AuthenticationRequest getPayload() {
        return payload;
    }

    public String getPersistentData() {
        return persistentData;
    }
}
