package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.Set;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;

public interface CredentialsEventRepositoryCustom extends Creatable {

    List<CredentialsEvent> findByUserIdAndCredentialsId(String userId, String credentialsId);

    List<CredentialsEvent> findByUserId(String userId);
    
    /**
     * @return a list ordered most recent first
     */
    List<CredentialsEvent> findMostRecentByUserIdAndCredentialsId(String userId, String credentialsId, int limit);

    List<CredentialsEvent> findMostRecentByUserIdAndCredentialsIdAndStatusIn(String userId, String credentialsId, int limit, Set<CredentialsStatus> statuses);
}
