package se.tink.backend.aggregation.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SupplementalInformationUtils {
    public static Map<String, Optional<String>> getResponseFields(
            String supplementalInformation, List<String> fieldNames) {
        Map<String, Optional<String>> responses = Maps.newHashMap();

        Map<String, String> answers =
                SerializationUtils.deserializeFromString(
                        supplementalInformation, new TypeReference<HashMap<String, String>>() {});

        for (String fieldName : fieldNames) {
            String answer = answers.get(fieldName);

            if (Strings.isNullOrEmpty(answer)) {
                responses.put(fieldName, Optional.empty());
            } else {
                responses.put(fieldName, Optional.of(answer));
            }
        }

        return responses;
    }

    public static Optional<String> getResponseFields(
            String supplementalInformation, String fieldName) {
        List<String> fieldNames = Lists.newArrayList();
        fieldNames.add(fieldName);

        Map<String, Optional<String>> responseMap =
                getResponseFields(supplementalInformation, fieldNames);

        return responseMap.get(fieldName);
    }
}
