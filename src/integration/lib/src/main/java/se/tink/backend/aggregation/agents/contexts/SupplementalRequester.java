package se.tink.backend.aggregation.agents.contexts;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;

public interface SupplementalRequester {

    void openBankId(String autoStartToken, boolean wait);

    String requestSupplementalInformation(
            Credentials credentials, long waitFor, TimeUnit unit, boolean wait);

    Optional<String> waitForSupplementalInformation(String mfaId, long waitFor, TimeUnit unit);

    default void openBankId() {
        openBankId(null, false);
    }

    default String requestSupplementalInformation(Credentials credentials) {
        return requestSupplementalInformation(credentials, true);
    }

    default String requestSupplementalInformation(Credentials credentials, boolean wait) {
        return requestSupplementalInformation(credentials, 2, TimeUnit.MINUTES, wait);
    }
}
