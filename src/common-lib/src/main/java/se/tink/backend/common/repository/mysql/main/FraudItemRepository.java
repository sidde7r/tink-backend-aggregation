package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import se.tink.backend.core.FraudItem;

public interface FraudItemRepository extends JpaRepository<FraudItem, String> {

    public List<FraudItem> findAllByUserId(String userId);

    @Transactional
    public void deleteByUserId(String userId);
}
