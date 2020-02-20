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

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            final String key, final long waitFor, final TimeUnit unit) {
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
