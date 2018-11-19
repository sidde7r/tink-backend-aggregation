package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

public class CryptoConfigurationImpl implements CryptoConfigurationCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<CryptoConfiguration> findByCryptoConfigurationIdClientName(String clientName) {
        return em.createQuery(
                "SELECT c FROM CryptoConfiguration c WHERE c.CryptoConfigurationId.clientname = :clientname",
                CryptoConfiguration.class)
                .setParameter("clientname", clientName)
                .getResultList();
    }

    @Override
    public CryptoConfiguration findByCryptoConfigurationId(CryptoConfigurationId cryptoId) {
        return em.find(CryptoConfiguration.class, cryptoId);
    }
}
