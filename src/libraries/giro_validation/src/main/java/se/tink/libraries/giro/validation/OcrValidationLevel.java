package se.tink.libraries.giro.validation;

/**
 * How the ocr validator should require giro message according to Swedish banking standards for OCR
 * types.
 * <li>{@link #NO_OCR}
 * <li>{@link #OCR_1_SOFT}
 * <li>{@link #OCR_2_HARD}
 * <li>{@link #OCR_3_HARD}
 * <li>{@link #OCR_4_HARD}
 */
public enum OcrValidationLevel {
    /** Allows only non-empty messages, no OCR */
    NO_OCR,
    /**
     * Allows both message and OCR of variable length. Validates OCR with Luhn check (last digit)
     */
    OCR_1_SOFT,
    /** Allows only OCR, variable length with Luhn check (last digit) */
    OCR_2_HARD,
    /**
     * Allows only OCR, variable length with length check (digit before last) and Luhn check (last
     * digit)
     */
    OCR_3_HARD,
    /** Allows only OCR, fixed length (one or two possible lengths) with Luhn check (last digit) */
    OCR_4_HARD
}
