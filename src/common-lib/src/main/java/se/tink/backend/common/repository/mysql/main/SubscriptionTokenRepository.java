package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.SubscriptionToken;

public interface SubscriptionTokenRepository extends JpaRepository<SubscriptionToken, String>, SubscriptionTokenRepositoryCustom {

    // A user can actually have multiple tokens. Should rarely happen, but possible. Returning a list here for
    // Optimistic Locking.
    List<SubscriptionToken> findAllByUserId(String id);

    @Transactional
    void deleteByUserId(String id);
}
