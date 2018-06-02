package se.tink.backend.insights.user;

import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface UserQueryService {
    User findById(UserId userId);
    Long getAmountCategorizationLevel(UserId userId);
    UserState getUserState(UserId userId);
    UserProfile getUserProfile(UserId userId);
}
