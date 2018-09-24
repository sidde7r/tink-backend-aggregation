package se.tink.backend.aggregation.provider.configuration.storage.repositories;

import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

public class ProviderStatusConfigurationRepositoryImpl implements ProviderStatusConfigurationRepositoryCustom {
        @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<ProviderStatusConfiguration> getProviderStatusConfiguration(String providerName) {
        List<ProviderStatusConfiguration> l = em.createQuery("SELECT s FROM ProviderStatusConfiguration s "
                + "WHERE s.providerName = :providername)"
                , ProviderStatusConfiguration.class)
                .setParameter("providername", providerName)
                .getResultList();
        if (l.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(l.get(0));
    }
}
