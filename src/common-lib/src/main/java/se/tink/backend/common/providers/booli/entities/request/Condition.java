package se.tink.backend.common.providers.booli.entities.request;

import java.util.Objects;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public enum Condition {
    BAD(ApplicationFieldOptionValues.CONDITION_BAD, 1),
    PRETTY_BAD(ApplicationFieldOptionValues.CONDITION_PRETTY_BAD, 2),
    OK(ApplicationFieldOptionValues.CONDITION_OK, 3),
    GOOD(ApplicationFieldOptionValues.CONDITION_GOOD, 4),
    EXCELLENT(ApplicationFieldOptionValues.CONDITION_EXCELLENT, 5);

    private final String optionValue;
    private final int number;

    Condition(String optionValue, int number) {
        this.optionValue = optionValue;
        this.number = number;
    }

    public Integer number() {
        return number;
    }

    public static Condition fromApplicationOptionValue(String optionValue) {
        for (Condition condition : values()) {
            if (Objects.equals(condition.optionValue, optionValue)) {
                return condition;
            }
        }

        return null;
    }

    public static Integer numberFromApplicationOptionValue(String optionValue) {
        Condition condition = fromApplicationOptionValue(optionValue);

        if (condition == null) {
            return null;
        }

        return condition.number;
    }
}
