package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import se.tink.backend.core.UserEvent;

public interface UserEventRepositoryCustom {
    
    public List<UserEvent> findMostRecentByUserId(String userId);
    
}
