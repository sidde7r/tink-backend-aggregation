package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

public class FortisRandomTokenGenerator {

    public static final String AXES = "en|TAB|fb|priv|TAB|";
    public static final String AXES_CHARS = "0123456789abcdef";

    public static final String CSRF_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";

    public String generateCSRF() {
        return RandomUtils.generateRandomAlphanumericString(128, CSRF_CHARS);
    }

    public String generateAxes() {
        return AXES + generateAxeUID() + "|";
    }

    public String generateDeviceId() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private String generateAxeUID() {
        return RandomUtils.generateRandomAlphanumericString(32, AXES_CHARS);
    }
}
