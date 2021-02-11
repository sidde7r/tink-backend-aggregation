package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;

public final class MockSupplementalRequester implements SupplementalRequester {

    private final Map<String, String> callbackData;

    public MockSupplementalRequester(Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public void requestSupplementalInformation(Credentials credentials) {
        // NOOP
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {

        /*
           TODO: Fix this hack. Probably we need to make SupplementInformationRequester payload
           have deterministic id so we will know here what to expect
        */
        if (mfaId.startsWith("tpcb")) {
            mfaId = "tpcb";
        }

        return Optional.ofNullable(callbackData.get(mfaId));
    }
}
