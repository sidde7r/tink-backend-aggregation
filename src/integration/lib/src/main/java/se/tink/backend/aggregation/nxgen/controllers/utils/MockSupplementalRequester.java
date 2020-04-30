package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;

public final class MockSupplementalRequester implements SupplementalRequester {

    private final String supplementalInfoForCredentials;
    private final Map<String, String> callbackData;

    public MockSupplementalRequester(
            String supplementalInfoForCredentials, Map<String, String> callbackData) {
        this.supplementalInfoForCredentials = supplementalInfoForCredentials;
        this.callbackData = callbackData;
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        // NOOP
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        return supplementalInfoForCredentials;
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {

        /*
           TODO: Fix this hack. Probably we need to make SupplementInformationRequester payload
           have deterministic id so we will know here what to expect
        */
        if (key.startsWith("tpcb")) {
            key = "tpcb";
        }

        return Optional.ofNullable(callbackData.get(key));
    }
}
