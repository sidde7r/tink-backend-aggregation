package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.strings.StringUtils;

public class SupplementalInformationFormer {

    private final Map<String, Field> supplementalInformation;

    public SupplementalInformationFormer(final Provider provider) {
        supplementalInformation =
                provider.getSupplementalFields().stream()
                        .collect(Collectors.toMap(Field::getName, field -> field));
    }

    public List<Field> formChallengeResponseFields(
            final Field.Key descriptionKey, final Field.Key inputKey, final String challenge) {
        List<Field> result = new ArrayList<>();

        Field description = getField(descriptionKey);
        final Field input = getField(inputKey);
        description.setValue(formatChallenge(challenge));

        result.add(description);
        result.add(input);

        return result;
    }

    public Field getField(final Field.Key key) {
        return getField(key.getFieldKey());
    }

    public Field getField(final String key) {
        Field field = supplementalInformation.get(key);
        if (field == null) {
            throw new IllegalStateException(
                    String.format("Missing %s supplemental information", key));
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
