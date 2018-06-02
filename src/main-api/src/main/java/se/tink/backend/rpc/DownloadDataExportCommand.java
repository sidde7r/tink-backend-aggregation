package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class DownloadDataExportCommand {

    private final UUID userId;
    private final UUID id;

    public DownloadDataExportCommand(String userId, String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        this.userId = UUIDUtils.fromString(userId);
        this.id = UUIDUtils.fromString(id);
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getId() {
        return id;
    }
}
