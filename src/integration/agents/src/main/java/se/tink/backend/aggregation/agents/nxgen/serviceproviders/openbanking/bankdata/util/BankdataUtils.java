package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.utils;

import java.util.Base64;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

public final class BankdataUtils {

    public static String generateCodeVerifier() {
        final byte[] code = RandomUtils.secureRandom(43);
        return Base64.getEncoder().withoutPadding().encodeToString(code);
    }

    public static String generateCodeChallenge(final String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Hash.sha256(input));
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }
}
