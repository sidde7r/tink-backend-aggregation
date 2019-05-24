package se.tink.backend.aggregation.nxgen.controllers.authentication;

import se.tink.backend.agents.rpc.Credentials;

public interface Credentialsable {

    Credentials getCredentials();
}
