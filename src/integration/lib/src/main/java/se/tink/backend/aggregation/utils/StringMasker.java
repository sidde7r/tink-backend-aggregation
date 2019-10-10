package se.tink.backend.aggregation.utils;

public interface StringMasker {
    String MASK = "***MASKED***";

    String getMasked(String string);
}
