package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;

/**
 * In progressive authentication, carry request information such as step, userInputs and credential.
 */
public final class AuthenticationRequestImpl implements AuthenticationRequest {
    private final String step;
    private final ImmutableList<String> userInputs;

    public AuthenticationRequestImpl(
            @Nonnull final String step, @Nonnull final List<String> userInputs) {
        this.step = Preconditions.checkNotNull(step);
        this.userInputs = ImmutableList.copyOf(userInputs);
    }

    @Override
    public String getStep() {
        return step;
    }

    @Override
    public ImmutableList<String> getUserInputs() {
        return userInputs;
    }
}
