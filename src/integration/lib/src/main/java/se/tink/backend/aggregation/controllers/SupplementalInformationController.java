package se.tink.backend.aggregation.controllers;

import com.google.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheScope;

public class SupplementalInformationController {
    private static final AggregationLogger log =
            new AggregationLogger(SupplementalInformationController.class);
    private final CacheClient cacheClient;
    private final CuratorFramework coordinationClient;

    @Inject
    SupplementalInformationController(
            CacheClient cacheClient, CuratorFramework coordinationClient) {
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
    }

    public void setSupplementalInformation(String credentialsId, String fields) {
        cacheClient.set(
                CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId, 60 * 10, fields);

        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(
                                BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, credentialsId));

        try {
            lock.removeBarrier();
        } catch (Exception e) {
            log.error("Could not remove barrier while supplementing credentials", e);
        }
    }

    public String getSupplementalInformation(String credentialsId) {
        try {
            return (String)
                    cacheClient.get(
                            CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId);
        } finally {
            cacheClient.delete(CacheScope.SUPPLEMENT_CREDENTIALS_BY_CREDENTIALSID, credentialsId);
        }
    }
}
