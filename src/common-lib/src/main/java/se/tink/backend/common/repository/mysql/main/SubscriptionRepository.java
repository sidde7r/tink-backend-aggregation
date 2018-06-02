package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Subscription;
import se.tink.backend.core.SubscriptionPk;

public interface SubscriptionRepository extends JpaRepository<Subscription, SubscriptionPk>, SubscriptionRepositoryCustom {

}
