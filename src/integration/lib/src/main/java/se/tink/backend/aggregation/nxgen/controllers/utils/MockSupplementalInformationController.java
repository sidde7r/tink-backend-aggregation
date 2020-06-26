package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

// TODO: We will need to mock this class for some agents. Implement later when we are working on
// these.
public final class MockSupplementalInformationController
        implements SupplementalInformationController {

    private final Map<String, String> callbackData;

    public MockSupplementalInformationController(Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String key, final long waitFor, final TimeUnit unit) {
        if (key.startsWith("tpcb")) {
            return Optional.of(callbackData);
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String> askSupplementalInformation(final Field... fields)
            throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void openThirdPartyApp(final ThirdPartyAppAuthenticationPayload payload) {
        // NOOP
    }
}
