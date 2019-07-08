package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import se.tink.backend.agents.rpc.Field;

/**
 * In progressive authentication, carry the intermediate step and fields. Yet to see if we need to
 * carry Credential object or any data in it.
 */
public final class AuthenticationResponse {

    private final ImmutableList<Field> fields;

    public AuthenticationResponse(@Nonnull List<Field> fields) {
        this.fields = ImmutableList.copyOf(fields);
    }

    public ImmutableList<Field> getFields() {
        return fields;
    }
}
