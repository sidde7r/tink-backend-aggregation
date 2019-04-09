package se.tink.backend.aggregation.agents.creditcards.ikano.api.errors;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public enum ErrorProvider {
    FATAL_ERRORS("INACTIVE_DEVICE", "INCORRECT_PARAMETER", "NO_SESSION", "UNHANDLED_ERROR");

    private final List<String> errors;

    ErrorProvider(String... errors) {
        this.errors = Lists.newArrayList(Arrays.asList(errors));
    }

    public static ResponseError.Type getTypeOf(String errorCode) {
        if (FATAL_ERRORS.errors.contains(errorCode)) {
            return ResponseError.Type.FATAL_ERROR;
        }

        return ResponseError.Type.USER_ERROR;
    }
}
