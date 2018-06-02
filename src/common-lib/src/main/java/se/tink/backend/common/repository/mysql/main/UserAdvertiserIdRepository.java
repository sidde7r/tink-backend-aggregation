package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.UserAdvertiserId;

import java.util.List;

public interface UserAdvertiserIdRepository extends JpaRepository<UserAdvertiserId, String> {
    public List<UserAdvertiserId> findByUserId(String userId);

    @Transactional
    public void deleteByUserId(String userId);
}
