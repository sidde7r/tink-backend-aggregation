package se.tink.backend.rpc.application;

import com.google.common.base.Strings;
import com.google.common.base.Preconditions;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class GetEligibleApplicationTypesCommand {
    private final UUID userId;

    public GetEligibleApplicationTypesCommand(String userId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        this.userId = UUIDUtils.fromString(userId);
    }

    public UUID getUserId() {
        return userId;
    }
}
