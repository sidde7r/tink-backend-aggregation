package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class TypedAuthenticationController implements Authenticator {
    private final Map<CredentialsTypes, TypedAuthenticator> authenticatorsByCredentialsType;

    public TypedAuthenticationController(TypedAuthenticator... authenticators) {
        this.authenticatorsByCredentialsType =
                Arrays.stream(authenticators)
                        .filter(a -> Preconditions.checkNotNull(a).getType() != null)
                        .collect(Collectors.toMap(TypedAuthenticator::getType, c -> c));

        Preconditions.checkState(
                !authenticatorsByCredentialsType.isEmpty(),
                "No valid TypedAuthenticators added through constructor");
    }

    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !authenticatorsByCredentialsType.containsKey(
                        Preconditions.checkNotNull(credentials.getType())),
                String.format(
                        "Authentication with CredentialsType %s have not yet been implemented",
                        credentials.getType()));

        authenticatorsByCredentialsType.get(credentials.getType()).authenticate(credentials);
    }
}
