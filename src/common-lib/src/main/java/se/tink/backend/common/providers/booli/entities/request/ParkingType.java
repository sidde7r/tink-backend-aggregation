package se.tink.backend.common.providers.booli.entities.request;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum ParkingType implements BooliParameterType {
    GARAGE("garage"), DOUBLE_GARAGE("double-garage"), DRIVEWAY("driveway"), STREET("on-street"), NONE("no");

    private final String value;

    ParkingType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public static ParkingType fromApplicationOptionValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        switch (value) {
        case ApplicationFieldOptionValues.PARKING_GARAGE:
            return GARAGE;
        case ApplicationFieldOptionValues.PARKING_DOUBLE_GARAGE:
            return DOUBLE_GARAGE;
        case ApplicationFieldOptionValues.PARKING_DRIVEWAY:
            return DRIVEWAY;
        case ApplicationFieldOptionValues.PARKING_STREET:
            return STREET;
        case ApplicationFieldOptionValues.NO:
            return NONE;
        default:
            return null;
        }
    }
}
