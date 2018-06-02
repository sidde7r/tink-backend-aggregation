package se.tink.backend.common.repository.cassandra;

import java.util.Date;
import java.util.List;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.UserLocation;

public interface UserLocationRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);
    
    List<UserLocation> findAllByUserId(String userId);
    
    List<UserLocation> findAllByUserIdAndDateBetween(String userId, Date from, Date to);
}
