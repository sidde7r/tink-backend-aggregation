package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

public class ProviderSessionCacheController {
    private static final Logger logger =
            LoggerFactory.getLogger(ProviderSessionCacheController.class);
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;

    @Inject
    ProviderSessionCacheController(CacheClient cacheClient, CuratorFramework coordinationClient) {
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
    }

    public void setProviderSessionCache(String financialInstitutionId, String value) {
        logger.info(
                "Received provider session information for financialInstitutionId: {}, cache client: {}",
                financialInstitutionId,
                cacheClient.getClass());
        cacheClient.set(
                CacheScope.PROVIDER_SESSION_BY_FINANCIALINSTITUTIONID,
                financialInstitutionId,
                60 * 15,
                value);

        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(
                                BarrierName.Prefix.PROVIDER_SESSION_INFORMATION,
                                financialInstitutionId));

        try {
            lock.removeBarrier();
        } catch (Exception e) {
            logger.error("Could not remove barrier while setting provider session information", e);
        }
    }

    public String getProviderSessionCache(String financialInstitutionId) {
        return (String)
                cacheClient.get(
                        CacheScope.PROVIDER_SESSION_BY_FINANCIALINSTITUTIONID,
                        financialInstitutionId);
    }
}
