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

    private static String getCacheKey(String appId, String financialInstitutionId) {
        return String.format("%s:%s", appId, financialInstitutionId);
    }

    public void setProviderSessionCache(
            String appId, String financialInstitutionId, String value, int expiredTimeInSeconds) {
        logger.info(
                "Received provider session information for appId: {} financialInstitutionId: {}."
                        + " This cache will be expired in {} seconds",
                appId,
                financialInstitutionId,
                expiredTimeInSeconds);

        if (expiredTimeInSeconds < 1) {
            // If expired time is 0, the cache is never expired
            expiredTimeInSeconds = 1;
        }

        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(
                                BarrierName.Prefix.PROVIDER_SESSION_INFORMATION,
                                getCacheKey(appId, financialInstitutionId)));

        try {
            lock.setBarrier();
            cacheClient.set(
                    CacheScope.PROVIDER_SESSION_BY_APPID_AND_FINANCIALINSTITUTIONID,
                    getCacheKey(appId, financialInstitutionId),
                    expiredTimeInSeconds,
                    value);
        } catch (Exception e) {
            logger.error("Error while setting provider session in cache", e);
        } finally {
            try {
                lock.removeBarrier();
            } catch (Exception e) {
                logger.error("Exception while trying to remove barrier", e);
            }
        }
    }

    public String getProviderSessionCache(String appId, String financialInstitutionId) {
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(
                                BarrierName.Prefix.PROVIDER_SESSION_INFORMATION,
                                getCacheKey(appId, financialInstitutionId)));

        try {
            lock.setBarrier();
            return (String)
                    cacheClient.get(
                            CacheScope.PROVIDER_SESSION_BY_APPID_AND_FINANCIALINSTITUTIONID,
                            getCacheKey(appId, financialInstitutionId));
        } catch (Exception e) {
            logger.error("Caught exception while getting provider session cache information", e);
            return null;
        } finally {
            try {
                lock.removeBarrier();
            } catch (Exception e) {
                logger.error("Exception while trying to remove barrier", e);
            }
        }
    }
}
