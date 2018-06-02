package se.tink.backend.core.exceptions;

public class InvalidSuggestLimitException extends IllegalArgumentException {
    public InvalidSuggestLimitException(int min, int max, int input) {
        super(String.format("Invalid suggest limit (Min = '%d', Max = '%d', Input = '%d')", min, max, input));
    }
}
