package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

public class NumpadDigitEntity {
    private final String key;
    private final Integer digit;

    public NumpadDigitEntity(String key, Integer digit) {
        this.key = key;
        this.digit = digit;
    }

    public String getKey() {
        return key;
    }

    public Integer getDigit() {
        return digit;
    }
}
