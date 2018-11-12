package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public class ClusterCryptoConfigurationImpl implements ClusterCryptoConfigurationCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ClusterCryptoConfiguration> findByCryptoIdClusterId(String clusterId) {
        return em.createQuery(
                "SELECT c FROM ClusterCryptoConfiguration c WHERE c.cryptoId.clusterId = :clusterid",
                ClusterCryptoConfiguration.class)
                .setParameter("clusterid", clusterId)
                .getResultList();
    }

    public ClusterCryptoConfiguration findByCryptoId(CryptoId cryptoId) {
        return em.find(ClusterCryptoConfiguration.class, cryptoId);
    }
}
