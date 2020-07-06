package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public final class MockSupplementalInformationHelper implements SupplementalInformationHelper {

    private final Map<String, String> callbackData;

    // TODO: generalize to allow for other types of supplemental information
    public MockSupplementalInformationHelper(final Map<String, String> callbackData) {
        this.callbackData = callbackData;
    }

    @Override
    public String waitForLoginInput() {
        return callbackData.get(Field.Key.LOGIN_INPUT.getFieldKey());
    }

    @Override
    public String waitForAddBeneficiaryInput() {
        return callbackData.get(Field.Key.ADD_BENEFICIARY_INPUT.getFieldKey());
    }

    @Override
    public String waitForOtpInput() {
        return callbackData.get(Field.Key.OTP_INPUT.getFieldKey());
    }

    @Override
    public String waitForLoginChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String waitForSignCodeChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String waitForSignForBeneficiaryChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String waitForTwoStepSignForBeneficiaryChallengeResponse(
            final String challenge, final String extraChallenge) throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String waitForSignForTransferChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String waitForTwoStepSignForTransferChallengeResponse(
            final String challenge, final String extraChallenge) throws SupplementalInfoException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void waitAndShowLoginDescription(final String description)
            throws SupplementalInfoException {
        // NOOP
    }

    @Override
    public Map<String, String> askSupplementalInformation(final Field... fields)
            throws SupplementalInfoException {
        Map<String, String> supplementalInformations = new HashMap<>();
        Stream.of(fields)
                .filter(field -> Objects.nonNull(field.getName()))
                .filter(field -> callbackData.containsKey(field.getName()))
                .forEach(
                        field ->
                                supplementalInformations.put(
                                        field.getName(), this.callbackData.get(field.getName())));
        return supplementalInformations;
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            final String key, final long waitFor, final TimeUnit unit) {

        return Optional.ofNullable(callbackData);
    }

    @Override
    public void openThirdPartyApp(final ThirdPartyAppAuthenticationPayload payload) {
        // NOOP
    }
}
