package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;

public final class MockSupplementalRequester implements SupplementalRequester {

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        // NOOP
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        throw new UnsupportedOperationException("Not yet supported for WireMock tests.");
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
        throw new UnsupportedOperationException("Not yet supported for WireMock tests.");
    }
}
