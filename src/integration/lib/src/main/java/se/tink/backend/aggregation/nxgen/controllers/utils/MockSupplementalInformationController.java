package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

// TODO: We will need to mock this class for some agents. Implement later when we are working on
// these.
public final class MockSupplementalInformationController
        implements SupplementalInformationController {

    private final Map<String, String> callbackData;
    private short interactionCounter;

    public MockSupplementalInformationController(Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String key, final long waitFor, final TimeUnit unit) {
        interactionCounter++;
        if (key.startsWith("tpcb")) {
            return Optional.of(callbackData);
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String> askSupplementalInformationSync(final Field... fields)
            throws SupplementalInfoException {
        interactionCounter++;
        return Stream.of(fields)
                .map(Field::getName)
                .filter(Objects::nonNull)
                .filter(this.callbackData::containsKey)
                .collect(Collectors.toMap(Function.identity(), this.callbackData::get));
    }

    @Override
    public void openThirdPartyApp(final ThirdPartyAppAuthenticationPayload payload) {
        // NOOP
    }

    @Override
    public short getInteractionCounter() {
        return interactionCounter;
    }
}
