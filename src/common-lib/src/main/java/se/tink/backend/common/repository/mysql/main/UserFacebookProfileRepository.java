package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.UserFacebookProfile;

@Repository
public interface UserFacebookProfileRepository extends JpaRepository<UserFacebookProfile, String>, UserFacebookProfileRepositoryCustom {
    public UserFacebookProfile findByProfileId(String profileId);

    public UserFacebookProfile findByUserId(String userId);
}
