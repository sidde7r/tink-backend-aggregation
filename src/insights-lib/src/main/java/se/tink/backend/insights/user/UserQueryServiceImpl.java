package se.tink.backend.insights.user;

import com.google.inject.Inject;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.user.cache.CachedUsers;

public class UserQueryServiceImpl implements UserQueryService {

    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private CachedUsers cache;

    @Inject
    public UserQueryServiceImpl(UserRepository userRepository,
            UserStateRepository userStateRepository, CachedUsers cache) {
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.cache = cache;
    }

    private User storeInCacheAndReturn(UserId userId, User user) {
        cache.save(userId, user);
        return user;
    }

    public User findById(UserId userId) {
        User user = userRepository.findOne(userId.value());
        if (user == null) {
            return null;
        }
        return storeInCacheAndReturn(userId, user);
    }

    @Override
    public Long getAmountCategorizationLevel(UserId userId) {
        UserState userState = userStateRepository.findOneByUserId(userId.value());
        if (userState == null) {
            return null;
        }
        return userState.getAmountCategorizationLevel();
    }

    @Override
    public UserState getUserState(UserId userId) {
        // TODO move all these to a User Object
        return userStateRepository.findOneByUserId(userId.value());
    }

    @Override
    public UserProfile getUserProfile(UserId userId) {
        return findById(userId).getProfile();
    }
}
