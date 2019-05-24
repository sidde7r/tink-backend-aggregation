package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class SteppableAuthenticationRequest implements AuthenticationSteppable {
    private final Class<? extends AuthenticationStep> step;
    private final ImmutableList<String> userInputs;

    private SteppableAuthenticationRequest(
            final Class<? extends AuthenticationStep> step,
            @Nonnull final List<String> userInputs) {
        this.step = step;
        this.userInputs = ImmutableList.copyOf(userInputs);
    }

    public static SteppableAuthenticationRequest initialRequest() {
        return new SteppableAuthenticationRequest(null, Collections.emptyList());
    }

    public static SteppableAuthenticationRequest subsequentRequest(
            @Nonnull final Class<? extends AuthenticationStep> klass,
            @Nonnull final List<String> userInputs) {
        return new SteppableAuthenticationRequest(Preconditions.checkNotNull(klass), userInputs);
    }

    @Override
    public Optional<Class<? extends AuthenticationStep>> getStep() {
        return Optional.ofNullable(step);
    }

    public ImmutableList<String> getUserInputs() {
        return userInputs;
    }
}
