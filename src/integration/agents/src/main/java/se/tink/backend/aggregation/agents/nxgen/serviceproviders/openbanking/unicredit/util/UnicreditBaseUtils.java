package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.util;

import java.util.Base64;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public final class UnicreditBaseUtils {

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    private UnicreditBaseUtils() {
        throw new AssertionError();
    }
}
