package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.AbnAmroSubscription;

@Repository
public interface AbnAmroSubscriptionRepository extends JpaRepository<AbnAmroSubscription, String> {

    AbnAmroSubscription findOneByUserId(String userId);

    @Transactional
    void deleteByUserId(String userId);
}
