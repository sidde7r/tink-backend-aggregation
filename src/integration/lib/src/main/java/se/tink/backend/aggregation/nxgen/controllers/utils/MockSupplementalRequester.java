package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class MockSupplementalRequester implements SupplementalRequester {

    private final Map<String, String> callbackData;

    public MockSupplementalRequester(Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        // NOOP
    }

    @Override
    public String requestSupplementalInformation(
            Credentials credentials, long waitFor, TimeUnit timeUnit, boolean wait) {
        return SerializationUtils.serializeToString(callbackData);
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
