package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.tink.backend.core.UserFacebookFriend;

@Repository
public interface UserFacebookFriendRepository extends JpaRepository<UserFacebookFriend, String>,
        UserFacebookFriendRepositoryCustom {
    public List<UserFacebookFriend> findByUserId(String userId);

    public List<UserFacebookFriend> findByProfileId(String userId);
}
