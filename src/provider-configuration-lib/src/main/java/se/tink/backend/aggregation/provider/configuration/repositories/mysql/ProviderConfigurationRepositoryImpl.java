package se.tink.backend.aggregation.provider.configuration.repositories.mysql;

import se.tink.backend.aggregation.provider.configuration.repositories.ProviderConfiguration;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ProviderConfigurationRepositoryImpl implements ProviderConfigurationRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName) {
        return em.createQuery("SELECT p FROM ProviderConfiguration p "
                        + "WHERE p.name IN (SELECT c.clusterProviderId.providerName FROM ClusterProviderConfiguration c "
                        + "WHERE c.clusterProviderId.clusterId = :clusterid "
                        + "AND c.clusterProviderId.providerName = :providername)", ProviderConfiguration.class)
                .setParameter("providername", providerName)
                .setParameter("clusterid", clusterId)
                .getSingleResult();
    }

    @Override
    public List<ProviderConfiguration> findAllByClusterIdAndMarket(String clusterId, String market) {
        return em.createQuery("SELECT p FROM ProviderConfiguration p WHERE p.market = :market AND p.name IN "
                + "(SELECT c.clusterProviderId.providerName FROM ClusterProviderConfiguration c "
                + "WHERE c.clusterProviderId.clusterId = :clusterid)", ProviderConfiguration.class)
                .setParameter("clusterid", clusterId)
                .setParameter("market", market)
                .getResultList();
    }

    @Override
    public List<ProviderConfiguration> findAllByClusterId(String clusterId) {
        return em.createQuery("SELECT p FROM ProviderConfiguration p WHERE name IN "
                + "(SELECT c.clusterProviderId.providerName FROM ClusterProviderConfiguration c "
                + "WHERE c.clusterProviderId.clusterId = :clusterid)", ProviderConfiguration.class)
                .setParameter("clusterid", clusterId)
                .getResultList();
    }

    @Override
    public List<ProviderConfiguration> findAllByMarket(String market) {
        return em.createQuery(
                "SELECT p FROM ProviderConfiguration p WHERE p.market = :market", ProviderConfiguration.class)
                .setParameter("market", market)
                .getResultList();
    }

    @Override
    public ProviderConfiguration findByName(String name) {
        return em.createQuery(
                "SELECT p FROM ProviderConfiguration p WHERE p.name = :name", ProviderConfiguration.class)
                .setParameter("name", name)
                .getSingleResult();
    }
}
