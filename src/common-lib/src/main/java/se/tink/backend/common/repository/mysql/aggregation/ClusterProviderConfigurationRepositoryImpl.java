package se.tink.backend.common.repository.mysql.aggregation;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.core.ClusterProviderConfiguration;

public class ClusterProviderConfigurationRepositoryImpl implements ClusterProviderConfigurationRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ClusterProviderConfiguration> findAllByClusterProviderIdClusterId(String clusterId) {
        return em.createQuery(
                "SELECT c FROM ClusterProviderConfiguration c WHERE c.providerId.clusterId = :clusterid",
                ClusterProviderConfiguration.class)
                .setParameter("clusterid", clusterId)
                .getResultList();
    }
}
