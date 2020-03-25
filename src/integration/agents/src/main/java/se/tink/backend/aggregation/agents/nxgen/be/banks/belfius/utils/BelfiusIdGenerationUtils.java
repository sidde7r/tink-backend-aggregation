package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BelfiusIdGenerationUtils {
    /* Methods below are copied from BelfiusSecurityUtils as the task is not about code refactor.
       TODO: During refactor - please change it to non-util class (make methods non-static).
       This will enable unit testing of the code, which uses them.
       However they are used mainly by DTOs (which is also a bad practice).
    */

    public static String generateTransactionId() {
        return "signIWSAuthentication"
                + System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateTransactionIdRegisterDevice() {
        return "signDeviceRegistration"
                + System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateTransferId() {
        return "submitTransfer"
                + System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateBeneficiaryId() {
        return "signBeneficiary"
                + System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateSignTransferId() {
        return "signTransfer"
                + System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (Math.random() * 1000000));
    }

    public static String generateDeviceToken() {
        return RandomStringUtils.random(53, "0123456789ABCDEF");
    }
}
