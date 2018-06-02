package se.tink.backend.common.repository.mysql.main;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;

public interface CredentialsRepository extends JpaRepository<Credentials, String>, CredentialsRepositoryCustom {

    public Credentials findOne(@Param("credentialsId") String credentialsId);

    public List<Credentials> findAllByStatus(CredentialsStatus status);

    public List<Credentials> findAllByStatusAndTypeIn(CredentialsStatus status, Set<CredentialsTypes> types);

    public List<Credentials> findAllByUserIdAndProviderName(
            @Param("userId") String userId, @Param("providerName") String providerName);

    public List<Credentials> findAllByUserIdAndType(
            @Param("userId") String userId, @Param("types") CredentialsTypes types);
    
    public List<Credentials> findAllByStatusIn(Collection<CredentialsStatus> status);
    
    public List<Credentials> findAllByType(CredentialsTypes type);
    
    public List<Credentials> findByStatusInAndStatusUpdatedLessThan(Set<CredentialsStatus> resetCredentialsStatuses,
            Date date);

    public long countByProviderNameInAndStatusInAndTypeNotIn(Collection<String> newHashSet,
            Set<CredentialsStatus> status, Set<CredentialsTypes> types);

    List<Credentials> findAllByPayload(@Param("payload") String payload);
}
