package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.libraries.i18n.LocalizableKey;

public enum NordeaFailures {
    ERROR_TOKEN_EXPIRED("error.token.expired"),
    ERROR_API_KEY_MISSING("error.apikey.missing"),
    ERROR_TOKEN("error.token");

    private String code;
    private static EnumMap<NordeaFailures, Function<String, Exception>>
            nordeaFailuresToExceptionCreatorsMapper = new EnumMap<>(NordeaFailures.class);

    static {
        nordeaFailuresToExceptionCreatorsMapper.put(
                ERROR_TOKEN_EXPIRED,
                description ->
                        SessionError.SESSION_EXPIRED.exception(new LocalizableKey(description)));
        nordeaFailuresToExceptionCreatorsMapper.put(
                ERROR_API_KEY_MISSING, description -> new IllegalArgumentException(description));
        nordeaFailuresToExceptionCreatorsMapper.put(
                ERROR_TOKEN, description -> new IllegalArgumentException(description));
    }

    NordeaFailures(String code) {
        this.code = code;
    }

    public static void mapNordeaFailureToException(String nordeaFailure, String description)
            throws Exception {
        throw nordeaFailuresToExceptionCreatorsMapper
                .get(fromString(nordeaFailure))
                .apply(description);
    }

    public static NordeaFailures fromString(String text) {
        return Arrays.stream(NordeaFailures.values())
                .filter(s -> s.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unrecognized Nordea failure code : " + text));
    }
}
