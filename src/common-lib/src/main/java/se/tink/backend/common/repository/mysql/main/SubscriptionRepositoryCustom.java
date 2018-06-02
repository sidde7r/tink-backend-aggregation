package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Subscription;

public interface SubscriptionRepositoryCustom {
    
    List<Subscription> findAllByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}
