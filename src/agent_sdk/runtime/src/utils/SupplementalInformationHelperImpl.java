package src.agent_sdk.runtime.src.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.agent.sdk.user_interaction.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.utils.SupplementalInformationHelper;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;

public class SupplementalInformationHelperImpl implements SupplementalInformationHelper {
    private final Map<String, Field> fieldMap;

    public SupplementalInformationHelperImpl(Map<String, Field> fieldMap) {
        this.fieldMap = fieldMap;
    }

    @Override
    public SupplementalInformation getMutableFields(Key... keys) throws SupplementalInfoException {
        List<Field> fieldsList =
                Arrays.stream(keys).map(this::getField).collect(Collectors.toList());
        return SupplementalInformation.from(fieldsList);
    }

    @Override
    public UserInteraction<SupplementalInformation> getFields(Key... keys)
            throws SupplementalInfoException {
        return UserInteraction.supplementalInformation(getMutableFields(keys))
                .userResponseRequired()
                .build();
    }

    @Override
    public UserInteraction<SupplementalInformation> getLoginInput()
            throws SupplementalInfoException {
        return UserInteraction.supplementalInformation(
                        SupplementalInformation.from(getField(Key.LOGIN_INPUT)))
                .userResponseRequired()
                .build();
    }

    @Override
    public UserInteraction<SupplementalInformation> getOtpInput() throws SupplementalInfoException {
        return UserInteraction.supplementalInformation(
                        SupplementalInformation.from(getField(Key.OTP_INPUT)))
                .userResponseRequired()
                .build();
    }

    @Override
    public UserInteraction<SupplementalInformation> getLoginChallengeResponse(String challenge)
            throws SupplementalInfoException {
        Field inputField = getField(Key.LOGIN_INPUT);
        Field descriptionField = getField(Key.LOGIN_DESCRIPTION);
        descriptionField.setValue(formatChallenge(challenge));

        return UserInteraction.supplementalInformation(
                        SupplementalInformation.from(descriptionField, inputField))
                .userResponseRequired()
                .build();
    }

    @Override
    public UserInteraction<SupplementalInformation> getSignCodeChallengeResponse(String challenge)
            throws SupplementalInfoException {
        Field inputField = getField(Key.SIGN_CODE_INPUT);
        Field descriptionField = getField(Key.SIGN_CODE_DESCRIPTION);
        descriptionField.setValue(formatChallenge(challenge));

        return UserInteraction.supplementalInformation(
                        SupplementalInformation.from(descriptionField, inputField))
                .userResponseRequired()
                .build();
    }

    private Field getField(final Field.Key key) throws IllegalStateException {
        Field field = fieldMap.get(key.getFieldKey());
        if (field == null) {
            throw new IllegalStateException(
                    String.format("Missing %s supplemental information", key.getFieldKey()));
        }
        return field;
    }

    /**
     * Inserts a space every 4th character of the challenge
     *
     * @param challenge
     * @return formatted challenge string
     */
    private String formatChallenge(final String challenge) {
        String trimmedChallenge = challenge.replaceAll("\\s+", "");
        return trimmedChallenge.replaceAll("(.{" + 4 + "})", "$1" + ' ');
    }
}
