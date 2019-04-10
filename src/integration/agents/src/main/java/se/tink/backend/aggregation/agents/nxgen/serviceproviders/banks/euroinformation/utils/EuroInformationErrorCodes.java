package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum EuroInformationErrorCodes {
    SUCCESS("0000"),
    LOGIN_ERROR("1000"),
    NOT_LOGGED_IN("0001"),
    TECHNICAL_PROBLEM("9000"),
    NO_ACCOUNT("2005"),
    NO_ENUM_VALUE("");

    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationErrorCodes.class);
    private final String codeNumber;

    EuroInformationErrorCodes(String s) {
        this.codeNumber = s;
    }

    public static EuroInformationErrorCodes getByCodeNumber(String number) {
        return Arrays.stream(EuroInformationErrorCodes.values())
                .filter(c -> c.codeNumber.equals(number))
                .findFirst()
                .orElseGet(
                        () -> {
                            LOGGER.info("Unknown error message: " + number);
                            return EuroInformationErrorCodes.NO_ENUM_VALUE;
                        });
    }

    public String getCodeNumber() {
        return codeNumber;
    }
}
