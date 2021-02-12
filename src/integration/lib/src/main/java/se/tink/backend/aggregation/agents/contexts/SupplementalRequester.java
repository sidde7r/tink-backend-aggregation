package se.tink.backend.aggregation.agents.contexts;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;

public interface SupplementalRequester {

    void requestSupplementalInformation(Credentials credentials);

    Optional<String> waitForSupplementalInformation(String mfaId, long waitFor, TimeUnit unit);
}
