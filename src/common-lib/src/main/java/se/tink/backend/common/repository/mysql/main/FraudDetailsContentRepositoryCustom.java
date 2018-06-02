package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.core.FraudDetailsContent;

public interface FraudDetailsContentRepositoryCustom {

    List<FraudDetailsContent> findByUserId(String userId, CacheClient cacheClient);
    
}
