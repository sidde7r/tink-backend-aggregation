package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.UserPublicKey;

@Repository
public interface UserPublicKeyRepository extends JpaRepository<UserPublicKey, String>, UserPublicKeyRepositoryCustom {
    Optional<UserPublicKey> findOptionalByIdAndUserId(String id, String userId);

    List<UserPublicKey> findByUserId(String userId);

    List<UserPublicKey> findByDeviceId(String deviceId);
}
