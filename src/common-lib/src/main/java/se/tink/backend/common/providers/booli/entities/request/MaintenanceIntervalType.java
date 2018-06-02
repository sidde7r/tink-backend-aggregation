package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum MaintenanceIntervalType implements BooliParameterType {
    RECENT("recent"), SOME_TIME_AGO("some-time-ago"), LONG_TIME_AGO("long-time-ago");

    private final String value;

    MaintenanceIntervalType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static MaintenanceIntervalType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        switch (value) {
        case ApplicationFieldOptionValues.YEARS_AGO_0_TO_15:
            return RECENT;
        case ApplicationFieldOptionValues.YEARS_AGO_15_TO_30:
            return SOME_TIME_AGO;
        case ApplicationFieldOptionValues.YEARS_AGO_30_OR_MORE:
            return LONG_TIME_AGO;
        default:
            return null;
        }
    }
}
