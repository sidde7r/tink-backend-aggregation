package se.tink.backend.aggregation.nxgen.controllers.utils;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SupplementalInformationFormer {

    private final Map<String, Field> supplementalInformation;

    public SupplementalInformationFormer(final Provider provider) {
        supplementalInformation =
                provider.getSupplementalFields()
                        .stream()
                        .collect(Collectors.toMap(Field::getName, field -> field));
    }

    public List<Field> formChallenageResponseFields(final String challenge) {
        return formChallenageResponseFields(
                Field.Key.SIGN_CODE_DESCRIPTION, Field.Key.SIGN_CODE_INPUT, challenge);
    }

    private List<Field> formChallenageResponseFields(
            final Field.Key descriptionKey, final Field.Key inputKey, final String challenge) {
        List<Field> result = new ArrayList<>();

        Field description = getField(descriptionKey);
        final Field input = getField(inputKey);
        description.setValue(formatChallenge(challenge));

        result.add(description);
        result.add(input);

        return result;
    }

    public Field getField(final Field.Key key) throws IllegalStateException {
        Field field = supplementalInformation.get(key.getFieldKey());
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
        return StringUtils.insertPeriodically(challenge.replaceAll("\\s+", ""), ' ', 4);
    }
}
