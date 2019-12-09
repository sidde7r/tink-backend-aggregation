package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils;

import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

public class CrosskeyUtils {
    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String randomStringNumber() {
        return Integer.toString(RandomUtils.randomInt(Integer.MAX_VALUE - 1));
    }
}
