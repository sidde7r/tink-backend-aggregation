package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import se.tink.backend.core.UserFacebookProfile;

public interface UserFacebookProfileRepositoryCustom {
    public void deleteByUserId(String userId);

    public List<UserFacebookProfile> findStale();
}
