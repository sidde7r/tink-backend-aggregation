package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

public class FortisRandomTokenGenerator {

    private static final String AXES = "en|TAB|fb|priv|TAB|";

    private static final String AXES_CHARS = "0123456789abcdef";

    private static final String CSRF_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";

    private final RandomValueGenerator randomValueGenerator;

    public FortisRandomTokenGenerator(RandomValueGenerator randomValueGenerator) {
        this.randomValueGenerator = randomValueGenerator;
    }

    public String generateCSRF() {
        return randomValueGenerator.generateRandomAlphanumeric(128, CSRF_CHARS);
    }

    public String generateAxes() {
        return AXES + generateAxeUID() + "|";
    }

    public String generateDeviceId() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }

    private String generateAxeUID() {
        return randomValueGenerator.generateRandomAlphanumeric(32, AXES_CHARS);
    }
}
