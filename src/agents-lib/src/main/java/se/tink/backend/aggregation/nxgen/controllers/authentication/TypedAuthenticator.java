package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.agents.rpc.CredentialsTypes;

public interface TypedAuthenticator extends Authenticator {
    CredentialsTypes getType();
}
