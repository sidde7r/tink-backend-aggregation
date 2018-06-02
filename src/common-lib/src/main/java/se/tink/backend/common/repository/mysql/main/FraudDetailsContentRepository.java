package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.FraudDetailsContentContainer;

@Repository
public interface FraudDetailsContentRepository extends JpaRepository<FraudDetailsContentContainer, String>, FraudDetailsContentRepositoryCustom {
    @Transactional
    public void deleteByUserId(String userId);
}
