package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
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
    public SupplementalInformationController(
            CacheClient cacheClient, CuratorFramework coordinationClient) {
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
    }

    public void setSupplementalInformation(String credentialsId, String fields) {
        logger.info("Received supplemental information for credentialsId: {}", credentialsId);

        cacheClient.set(
                CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId, 60 * 10, fields);

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
            cacheClient.delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId);
        }
    }
}
