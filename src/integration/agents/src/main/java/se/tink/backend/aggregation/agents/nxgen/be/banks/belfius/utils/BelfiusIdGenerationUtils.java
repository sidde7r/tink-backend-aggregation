package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils;

import java.security.SecureRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BelfiusIdGenerationUtils {
    /* Methods below are copied from BelfiusSecurityUtils as the task is not about code refactor.
       TODO: During refactor - please change it to non-util class (make methods non-static).
       This will enable unit testing of the code, which uses them.
       However they are used mainly by DTOs (which is also a bad practice).
    */
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateTransactionId() {
        return "signIWSAuthentication" + buildSuffix();
    }

    public static String generateTransactionIdRegisterDevice() {
        return "signDeviceRegistration" + buildSuffix();
    }

    public static String generateTransferId() {
        return "submitTransfer" + buildSuffix();
    }

    public static String generateBeneficiaryId() {
        return "signBeneficiary" + buildSuffix();
    }

    public static String generateSignTransferId() {
        return "signTransfer" + buildSuffix();
    }

    private static String buildSuffix() {
        return System.currentTimeMillis() / 1000
                + "."
                + String.format("%06d", (int) (RANDOM.nextDouble() * 1000000));
    }

    public static String generateDeviceToken() {
        return RandomUtils.generateRandomAlphanumericString(53, "0123456789ABCDEF");
    }
}
