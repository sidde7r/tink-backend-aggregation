package se.tink.backend.utils;

import java.util.UUID;
import se.tink.libraries.uuid.UUIDUtils;

public class StringUtils extends se.tink.libraries.strings.StringUtils {
    /**
     * Helper function to generate a UUID without dashes.
     * <p>
     * TODO: Migrate to UUIDUtils. Feels more natural there.
     *
     * @return
     */
    public static String generateUUID() {
        return UUIDUtils.toTinkUUID(UUID.randomUUID());
    }
}
