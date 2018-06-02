package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import se.tink.backend.rpc.Feedback;

@Repository
@Transactional
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    
}
