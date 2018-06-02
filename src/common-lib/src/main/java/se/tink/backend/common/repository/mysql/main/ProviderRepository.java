package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Provider;

@Deprecated
public interface ProviderRepository extends JpaRepository<Provider, String>, ProviderRepositoryCustom {
    @Deprecated
    public Provider findByName(String name);
}
