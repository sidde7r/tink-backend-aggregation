package se.tink.backend.common.repository.cassandra;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.UserProfileData;

public interface UserProfileDataRepositoryCustom extends Creatable {
    List<UserProfileData> findAllByUserId(String userId);
    
    ImmutableMap<String, UserProfileData> getValuesByNameForUserId(String userId);
    
    void deleteByUserId(String userId);
}
