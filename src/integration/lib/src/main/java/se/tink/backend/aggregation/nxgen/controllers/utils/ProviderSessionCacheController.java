package se.tink.backend.aggregation.nxgen.controllers.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ProviderSessionCacheController {
    private static final Logger logger =
            LoggerFactory.getLogger(ProviderSessionCacheController.class);

    private ProviderSessionCacheContext providerSessionCacheContext;

    public ProviderSessionCacheController(ProviderSessionCacheContext providerSessionCacheContext) {
        this.providerSessionCacheContext = providerSessionCacheContext;
    }

    public void setProviderSessionCacheInfoWithExpiredTime(
            Map<String, String> fields, int expiredTimeInSeconds) {
        providerSessionCacheContext.setProviderSessionCache(
                SerializationUtils.serializeToString(fields), expiredTimeInSeconds);
        logger.info("Finished setting provider session cache information");
    }

    public void setProviderSessionCacheInfo(Map<String, String> fields) {
        setProviderSessionCacheInfoWithExpiredTime(fields, 10 * 60);
    }

    public Optional<Map<String, String>> getProviderSessionCacheInformation() {
        return Optional.ofNullable(providerSessionCacheContext.getProviderSessionCache())
                .map(ProviderSessionCacheController::stringToMap);
    }

    private static Map<String, String> stringToMap(final String string) {
        return SerializationUtils.deserializeFromString(
                string, new TypeReference<HashMap<String, String>>() {});
    }
}
