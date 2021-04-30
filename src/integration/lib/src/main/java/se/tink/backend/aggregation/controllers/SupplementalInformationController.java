package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

@Slf4j
public class SupplementalInformationController {
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;

    @Inject
    SupplementalInformationController(
            CacheClient cacheClient, CuratorFramework coordinationClient) {
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
    }

    public void setSupplementalInformation(String mfaId, String fields) {
        log.info("Received supplemental information for mfaId: {}", mfaId);

        Future<?> future =
                cacheClient.set(
                        CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, mfaId, 60 * 10, fields);

        log.info(
                "CacheClient cached the mfaId: {} with value status {}",
                mfaId,
                fields == null ? "null value" : "non null value");
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, mfaId));
        try {
            if (future != null) {
                future.get(5, TimeUnit.SECONDS);
            }
        } catch (TimeoutException e) {
            log.error(
                    "[SupplementalInformationController] Timeout exception when writing to cache.",
                    e);
        } catch (Exception e) {
            log.error(
                    "[SupplementalInformationController] Unhandled exception when writing to cache",
                    e);
            Thread.currentThread().interrupt();
        }

        try {
            lock.removeBarrier();
        } catch (Exception e) {
            log.error("Could not remove barrier while supplementing credentials", e);
        }

        log.info("DistributedBarrier removed for mfaId: {}", mfaId);
    }

    public String getSupplementalInformation(String mfaId) {
        try {
            Object cacheResult =
                    cacheClient.get(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, mfaId);

            log.info(
                    "CacheClient find the cache for mfaId: {} with value status {}",
                    mfaId,
                    cacheResult == null ? "null value" : "non null value");
            return (String) cacheResult;
        } catch (Exception e) {
            log.error("Could not fetch value for the mfaId {} with error", mfaId, e);
            throw e;
        } finally {
            log.info("CacheClient clear the cache for mfaId: {}", mfaId);
            try {
                Future<Boolean> future =
                        cacheClient.delete(
                                CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, mfaId);
                if (future == null || !future.get(10, TimeUnit.SECONDS)) {
                    log.error("Failed to clear the cache for {}", mfaId);
                }
            } catch (Exception e) {
                log.error("Failed to clear the cache for {}", mfaId, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
