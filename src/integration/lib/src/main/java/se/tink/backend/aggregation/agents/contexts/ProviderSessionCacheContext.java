package se.tink.backend.aggregation.agents.contexts;

public interface ProviderSessionCacheContext {
    String getProviderSessionCache();

    void setProviderSessionCache(String value, int expiredTimeInSeconds);
}
