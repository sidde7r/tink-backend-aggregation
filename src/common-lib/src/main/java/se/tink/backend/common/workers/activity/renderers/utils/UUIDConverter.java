package se.tink.backend.common.workers.activity.renderers.utils;

import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class UUIDConverter {
    public String convert(UUID uuid) {
        return UUIDUtils.toTinkUUID(uuid);
    }
}
