package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.type.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TypedAuthenticationController implements Authenticator, AuthenticationControllerType {
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

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        TypedAuthenticator typedAuthenticator =
                authenticatorsByCredentialsType.get(request.getCredentials().getType());
        if (Objects.nonNull(typedAuthenticator)
                && typedAuthenticator instanceof AuthenticationControllerType) {
            return ((AuthenticationControllerType) typedAuthenticator)
                    .isManualAuthentication(request);
        }

        return false;
    }
}
