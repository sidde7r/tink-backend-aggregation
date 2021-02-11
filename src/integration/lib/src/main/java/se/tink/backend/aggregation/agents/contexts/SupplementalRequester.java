package se.tink.backend.aggregation.agents.contexts;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;

public interface SupplementalRequester {

    String requestSupplementalInformation(
            Credentials credentials, long waitFor, TimeUnit unit, boolean wait);

    Optional<String> waitForSupplementalInformation(String mfaId, long waitFor, TimeUnit unit);

    default String requestSupplementalInformation(Credentials credentials, boolean wait) {
        return requestSupplementalInformation(credentials, 2, TimeUnit.MINUTES, wait);
    }
}
