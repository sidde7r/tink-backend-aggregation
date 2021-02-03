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

    public MockSupplementalInformationController(Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String mfaId, final long waitFor, final TimeUnit unit) {
        if (mfaId.startsWith("tpcb")) {
            return Optional.of(callbackData);
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String> askSupplementalInformationSync(final Field... fields)
            throws SupplementalInfoException {
        return Stream.of(fields)
                .map(Field::getName)
                .filter(Objects::nonNull)
                .filter(this.callbackData::containsKey)
                .collect(Collectors.toMap(Function.identity(), this.callbackData::get));
    }

    @Override
    public String askSupplementalInformationAsync(Field... fields) {
        return null;
    }

    @Override
    public Optional<Map<String, String>> openThirdPartyAppSync(
            ThirdPartyAppAuthenticationPayload payload) {
        return Optional.empty();
    }

    @Override
    public String openThirdPartyAppAsync(final ThirdPartyAppAuthenticationPayload payload) {
        // NOOP
        return null;
    }

    @Override
    public void openMobileBankIdSync(String autoStartToken) {
        // NOOP
    }

    @Override
    public String openMobileBankIdAsync(String autoStartToken) {
        return null;
    }
}
