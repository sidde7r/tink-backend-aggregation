package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

public class SupplementalInformationController {
    private static final Logger logger =
            LoggerFactory.getLogger(SupplementalInformationController.class);
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;

    @Inject
    SupplementalInformationController(
            CacheClient cacheClient, CuratorFramework coordinationClient) {
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
    }

    public void setSupplementalInformation(String credentialsId, String fields) {
        logger.info("Received supplemental information for credentialsId: {}", credentialsId);

        Future<?> future =
                cacheClient.set(
                        CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID,
                        credentialsId,
                        60 * 10,
                        fields);

        logger.info(
                "CacheClient cached the credentialsId: {} with value status {}",
                credentialsId,
                fields == null ? "null value" : "non null value");
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(
                                BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, credentialsId));
        try {
            if (future != null) {
                future.get(5, TimeUnit.SECONDS);
            }
        } catch (TimeoutException e) {
            logger.error(
                    "[SupplementalInformationController] Timeout exception when writing to cache.",
                    e);
        } catch (Exception e) {
            logger.error(
                    "[SupplementalInformationController] Unhandled exception when writing to cache",
                    e);
        }

        try {
            lock.removeBarrier();
        } catch (Exception e) {
            logger.error("Could not remove barrier while supplementing credentials", e);
        }

        logger.info("DistributedBarrier removed for credentialsId: {}", credentialsId);
    }

    public String getSupplementalInformation(String credentialsId) {
        try {
            Object cacheResult =
                    cacheClient.get(
                            CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId);

            logger.info(
                    "CacheClient find the cache for credentialsId: {} with value status {}",
                    credentialsId,
                    cacheResult == null ? "null value" : "non null value");
            return (String) cacheResult;
        } catch (Exception e) {
            logger.error(
                    "Could not fetch value for the credentialsId {} with error", credentialsId, e);
            throw e;
        } finally {
            logger.info("CacheClient clear the cache for credentialsId: {}", credentialsId);
            try {
                Future<Boolean> future =
                        cacheClient.delete(
                                CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId);
                if (future == null || !future.get(10, TimeUnit.SECONDS)) {
                    logger.error("Failed to clear the cache for {}", credentialsId);
                }
            } catch (Exception e) {
                logger.error("Failed to clear the cache for {}", credentialsId, e);
            }
        }
    }
}
