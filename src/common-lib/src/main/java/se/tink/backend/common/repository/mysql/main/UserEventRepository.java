package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.UserEvent;
import se.tink.backend.core.UserEventTypes;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long>, UserEventRepositoryCustom {
    
    public List<UserEvent> findAllByUserIdAndType(String userId, UserEventTypes type);
    
    public List<UserEvent> findAllByUserIdOrderByDateDesc(String userId, Pageable pageRequest);
    
}
