package se.tink.backend.aggregation.agents;

import se.tink.backend.agents.rpc.Credentials;

public interface ManualOrAutoAuth {
    boolean isManualAuthentication(Credentials credentials);
}
