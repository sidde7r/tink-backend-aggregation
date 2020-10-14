package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;

public class IngMiscUtils {

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static ProxyRequestHeaders constructInfoHeaders() {
        return ProxyRequestHeaders.builder()
                .appVersion(Headers.APP_VERSION_VALUE)
                .appIdentifier(Headers.APP_IDENTIFIER_VALUE)
                .deviceModel(Headers.DEVICE_MODEL_VALUE)
                .osVersion(Headers.OS_VERSION_VALUE)
                .devicePlatform(Headers.DEVICE_PLATFORM_VALUE)
                .build();
    }

}
