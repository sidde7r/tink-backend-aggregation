package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils;

import java.util.Arrays;
import se.tink.backend.aggregation.log.AggregationLogger;

public enum TargoBankErrorCodes {
    SUCCESS("0000"),
    LOGIN_ERROR("1000"),
    NOT_LOGGED_IN("0001"),
    TECHNICAL_PROBLEM("9000"),
    NO_ENUM_VALUE("");

    private static final AggregationLogger LOGGER = new AggregationLogger(TargoBankErrorCodes.class);
    private final String codeNumber;

    TargoBankErrorCodes(String s) {
        this.codeNumber = s;
    }

    public static TargoBankErrorCodes getByCodeNumber(String number) {
        return Arrays.stream(TargoBankErrorCodes.values()).filter(c -> c.codeNumber.equals(number)).findFirst()
                .orElseGet(() -> {
                    LOGGER.info("Unknown error message: " + number);
                    return TargoBankErrorCodes.NO_ENUM_VALUE;
                });
    }

    public String getCodeNumber() {
        return codeNumber;
    }
}
