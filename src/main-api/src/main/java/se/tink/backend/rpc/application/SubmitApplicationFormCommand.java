package se.tink.backend.rpc.application;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Optional;
import java.util.UUID;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;

public class SubmitApplicationFormCommand {
    private final UUID userId;
    private final UUID applicationId;
    private final User user;
    private final TinkUserAgent tinkUserAgent;

    public SubmitApplicationFormCommand(String applicationId, User user, Optional<String> userAgent) {
        validate(applicationId, user);

        this.userId = UUIDUtils.fromString(user.getId());
        this.applicationId = UUIDUtils.fromString(applicationId);
        this.user = user;
        this.tinkUserAgent = TinkUserAgent.of(userAgent);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public User getUser() {
        return user;
    }

    public TinkUserAgent getTinkUserAgent() {
        return tinkUserAgent;
    }

    private void validate(String applicationId, User user) {
        Preconditions.checkNotNull(user);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationId));
    }
}
