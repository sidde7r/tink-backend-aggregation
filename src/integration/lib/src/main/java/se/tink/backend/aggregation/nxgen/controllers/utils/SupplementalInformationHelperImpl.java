package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.strings.StringUtils;

public class SupplementalInformationHelperImpl implements SupplementalInformationHelper {

    private final Map<String, Field> supplementalInformation;
    private final SupplementalInformationController supplementalInformationController;

    /**
     * Do not construct your own SupplementalInfomationHelper. Use the instance available to your
     * agent from SubsequentGenerationAgent instead. Or even better, migrate to AgentPlatformAgent
     * or SubsequentProgressiveGenerationAgent where the Supplemental information controlling is
     * outside of the agent and you do not need to have an instance.
     */
    public SupplementalInformationHelperImpl(
            final Provider provider,
            final SupplementalInformationController supplementalInformationController) {
        supplementalInformation =
                provider.getSupplementalFields().stream()
                        .collect(Collectors.toMap(Field::getName, field -> field));
        this.supplementalInformationController = supplementalInformationController;
    }

    private Field getField(final Field.Key key) throws IllegalStateException {
        Field field = supplementalInformation.get(key.getFieldKey());
        if (field == null) {
            throw new IllegalStateException(
                    String.format("Missing %s supplemental information", key.getFieldKey()));
        }
        return field;
    }

    @Override
    public String waitForLoginInput() throws SupplementalInfoException {
        return waitForSupplementalInput(Field.Key.LOGIN_INPUT);
    }

    @Override
    public String waitForAddBeneficiaryInput() throws SupplementalInfoException {
        return waitForSupplementalInput(Field.Key.ADD_BENEFICIARY_INPUT);
    }

    @Override
    public String waitForOtpInput() throws SupplementalInfoException {
        return waitForSupplementalInput(Field.Key.OTP_INPUT);
    }

    @Override
    public String waitForLoginChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        return waitForChallengeResponse(
                Field.Key.LOGIN_DESCRIPTION, Field.Key.LOGIN_INPUT, challenge);
    }

    @Override
    public String waitForSignCodeChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        return waitForChallengeResponse(
                Field.Key.SIGN_CODE_DESCRIPTION, Field.Key.SIGN_CODE_INPUT, challenge);
    }

    @Override
    public String waitForSignForBeneficiaryChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        return waitForChallengeResponse(
                Field.Key.SIGN_FOR_BENEFICIARY_DESCRIPTION,
                Field.Key.SIGN_FOR_BENEFICIARY_INPUT,
                challenge);
    }

    @Override
    public String waitForTwoStepSignForBeneficiaryChallengeResponse(
            final String challenge, final String extraChallenge) throws SupplementalInfoException {
        return waitForChallengeResponseExtra(
                Field.Key.SIGN_FOR_BENEFICIARY_DESCRIPTION,
                Field.Key.SIGN_FOR_BENEFICIARY_EXTRA_DESCRIPTION,
                Field.Key.SIGN_FOR_BENEFICIARY_INPUT,
                challenge,
                extraChallenge);
    }

    @Override
    public String waitForSignForTransferChallengeResponse(final String challenge)
            throws SupplementalInfoException {
        return waitForChallengeResponse(
                Field.Key.SIGN_FOR_TRANSFER_DESCRIPTION,
                Field.Key.SIGN_FOR_TRANSFER_INPUT,
                challenge);
    }

    @Override
    public String waitForTwoStepSignForTransferChallengeResponse(
            final String challenge, final String extraChallenge) throws SupplementalInfoException {
        return waitForChallengeResponseExtra(
                Field.Key.SIGN_FOR_TRANSFER_DESCRIPTION,
                Field.Key.SIGN_FOR_TRANSFER_EXTRA_DESCRIPTION,
                Field.Key.SIGN_FOR_TRANSFER_INPUT,
                challenge,
                extraChallenge);
    }

    @Override
    public void waitAndShowLoginDescription(final String description)
            throws SupplementalInfoException {
        Field descriptionField = getField(Field.Key.LOGIN_DESCRIPTION);
        descriptionField.setValue(description);
        supplementalInformationController.askSupplementalInformationSync(descriptionField);
    }

    private String waitForChallengeResponseExtra(
            final Field.Key descriptionKey,
            final Field.Key extraDescriptionKey,
            final Field.Key inputKey,
            final String challenge,
            final String extraChallenge)
            throws SupplementalInfoException {
        Field description = getField(descriptionKey);
        Field extraDescription = getField(extraDescriptionKey);
        final Field input = getField(inputKey);
        description.setValue(formatChallenge(challenge));
        extraDescription.setValue(formatChallenge(extraChallenge));
        return waitForSupplementalDescriptionAndInput(
                inputKey, description, extraDescription, input);
    }

    private String waitForChallengeResponse(
            final Field.Key descriptionKey, final Field.Key inputKey, final String challenge)
            throws SupplementalInfoException {
        Field description = getField(descriptionKey);
        final Field input = getField(inputKey);
        description.setValue(formatChallenge(challenge));
        return waitForSupplementalDescriptionAndInput(inputKey, description, input);
    }

    private String waitForSupplementalInput(Field.Key inputKey) throws SupplementalInfoException {
        Field input = getField(inputKey);
        return supplementalInformationController
                .askSupplementalInformationSync(input)
                .get(inputKey.getFieldKey());
    }

    private String waitForSupplementalDescriptionAndInput(
            final Field.Key resultKey, final Field... fields) throws SupplementalInfoException {
        return supplementalInformationController
                .askSupplementalInformationSync(fields)
                .get(resultKey.getFieldKey());
    }

    /**
     * Inserts a space every 4th character of the challenge
     *
     * @param challenge
     * @return formatted challenge string
     */
    private String formatChallenge(final String challenge) {
        return StringUtils.insertPeriodically(challenge.replaceAll("\\s+", ""), ' ', 4);
    }

    @Override
    public Map<String, String> askSupplementalInformation(Field... fields)
            throws SupplementalInfoException {
        return supplementalInformationController.askSupplementalInformationSync(fields);
    }

    @Override
    public Optional<Map<String, String>> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
        return supplementalInformationController.waitForSupplementalInformation(key, waitFor, unit);
    }

    @Override
    public void openThirdPartyApp(ThirdPartyAppAuthenticationPayload payload) {
        supplementalInformationController.openThirdPartyAppAsync(payload);
    }
}
