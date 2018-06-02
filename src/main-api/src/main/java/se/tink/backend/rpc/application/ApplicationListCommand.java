package se.tink.backend.rpc.application;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationListCommand {
    private final UUID userId;
    private final User user;
    private final TinkUserAgent tinkUserAgent;

    public ApplicationListCommand(User user, Optional<String> userAgent) {
        Preconditions.checkNotNull(user, "User must not be null.");

        this.user = user;
        this.userId = UUIDUtils.fromString(user.getId());
        this.tinkUserAgent = TinkUserAgent.of(userAgent);
    }

    public User getUser() {
        return user;
    }

    public UUID getUserId() {
        return userId;
    }

    public TinkUserAgent getTinkUserAgent() {
        return tinkUserAgent;
    }
}
