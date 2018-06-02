package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.Provider;
@Deprecated
public interface ProviderRepositoryCustom {
    @Deprecated
    public List<Provider> findAll();
    @Deprecated
    public List<Provider> findProvidersByMarket(String market);
}
