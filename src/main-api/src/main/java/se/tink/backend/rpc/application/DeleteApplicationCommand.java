package se.tink.backend.rpc.application;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class DeleteApplicationCommand {
    private final UUID userId;
    private final UUID applicationId;

    public DeleteApplicationCommand(String userId, String applicationId) {
        validate(userId, applicationId);
        this.userId = UUIDUtils.fromString(userId);
        this.applicationId = UUIDUtils.fromString(applicationId);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    private void validate(String userId, String applicationId){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationId));
    }
}
