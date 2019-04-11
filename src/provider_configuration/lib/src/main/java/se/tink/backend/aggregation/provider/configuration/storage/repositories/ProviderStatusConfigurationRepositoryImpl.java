package se.tink.backend.aggregation.provider.configuration.storage.repositories;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ProviderStatusConfigurationRepositoryImpl
        implements ProviderStatusConfigurationRepositoryCustom {
    @PersistenceContext private EntityManager em;
}
