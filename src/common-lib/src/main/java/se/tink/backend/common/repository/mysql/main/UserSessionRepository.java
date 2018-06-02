package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.UserSession;

@Repository
public interface UserSessionRepository extends PagingAndSortingRepository<UserSession, String> {
    public List<UserSession> findByUserId(String userId);
}
