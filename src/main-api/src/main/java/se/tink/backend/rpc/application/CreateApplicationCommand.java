package se.tink.backend.rpc.application;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.libraries.application.ApplicationType;

public class CreateApplicationCommand {
    private final User user;
    private final TinkUserAgent tinkUserAgent;
    private final ApplicationType applicationType;

    public CreateApplicationCommand(User user, Optional<String> userAgent, ApplicationType applicationType) {
        validate(user, applicationType);

        this.user = user;
        this.tinkUserAgent = TinkUserAgent.of(userAgent);
        this.applicationType = applicationType;
    }

    public User getUser() {
        return user;
    }

    public TinkUserAgent getTinkUserAgent() {
        return tinkUserAgent;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    private void validate(User user, ApplicationType applicationType) {
        Preconditions.checkNotNull(user, "User must not be null.");
        Preconditions.checkNotNull(applicationType, "Application type is not set.");
    }
}
