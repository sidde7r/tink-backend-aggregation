package se.tink.backend.core.enums;

public enum ApplicationFieldType {    
    CHECKBOX,       // Checkbox.
    DATE,           // Date.
    EMAIL,          // Email.
    HIDDEN,         // Hidden. A default value should be submitted as the value.
    MULTI_SELECT,   // Select zero, one or many of pre-defined options.
    NUMBER,         // Numeric value, formatted as a number (with thousand separators).
    NUMERIC,        // Numeric value, without any formatting.
    SELECT,         // Select zero or one of pre-defined options.
    SIGNATURE,      // A signature represented by an ordered list of coordinates
    TEXT;           // Arbitrary text input.

    public static final String DOCUMENTED = 
                    "CHECKBOX, " +
                    "DATE, " +
                    "EMAIL, " +
                    "HIDDEN, " +
                    "MULTI_SELECT, " +
                    "NUMBER, " +
                    "NUMERIC, " +
                    "SELECT, " +
                    "SIGNATURE, " +
                    "TEXT";
}
