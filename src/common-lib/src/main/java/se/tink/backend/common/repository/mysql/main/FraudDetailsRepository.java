package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;

public interface FraudDetailsRepository extends JpaRepository<FraudDetails, String>, FraudDetailsRepositoryCustom {

    public List<FraudDetails> findAllByUserId(String userId);

    public List<FraudDetails> findAllByUserIdAndType(String userId, FraudDetailsContentType type);

    public List<FraudDetails> findAllByUserIdAndFraudItemId(String userId, String fraudItemId);
    
    @Transactional
    public void deleteByUserId(String userId);

    public List<FraudDetails> findAllByType(FraudDetailsContentType type);
    
    public List<FraudDetails> findAllByUserIdOrderByCreatedAsc(String userId);

}
