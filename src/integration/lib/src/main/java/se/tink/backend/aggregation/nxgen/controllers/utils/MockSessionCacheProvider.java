package se.tink.backend.aggregation.nxgen.controllers.utils;

import java.util.Map;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public final class MockSessionCacheProvider implements ProviderSessionCacheContext {

    private final Map<String, String> cache;

    @Override
    public String getProviderSessionCache() {
        return SerializationUtils.serializeToString(cache);
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        // NOOP
    }
}
