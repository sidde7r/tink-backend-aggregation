package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TargoBankErrorCodes {
    SUCCESS("0000"),
    LOGIN_ERROR("1000"),
    NOT_LOGGED_IN("0001"),
    TECHNICAL_PROBLEM("9000"),
    NO_ENUM_VALUE("");

    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankErrorCodes.class);
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
