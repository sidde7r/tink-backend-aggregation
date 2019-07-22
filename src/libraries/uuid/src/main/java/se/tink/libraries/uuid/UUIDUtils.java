package se.tink.libraries.uuid;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDUtils {
    public static final Function<UUID, String> TO_TINK_UUID_TRANSFORMER = UUIDUtils::toTinkUUID;
    private static final Pattern UUID_V4_PATTERN =
            Pattern.compile(
                    "[a-fA-F0-9]{8}-?[a-fA-F0-9]{4}-?4[a-fA-F0-9]{3}-?[89aAbB][a-fA-F0-9]{3}-?[a-fA-F0-9]{12}");
    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("[a-f0-9]{32}");
    public static final Function<String, UUID> FROM_TINK_UUID_TRANSFORMER = UUIDUtils::fromTinkUUID;

    public static boolean isValidTinkUUID(String uuid) {
        if (uuid == null) {
            return false;
        }
        return HEXADECIMAL_PATTERN.matcher(uuid).matches();
    }

    public static boolean isValidUUIDv4(String uuid) {
        if (uuid == null) {
            return false;
        }
        return UUID_V4_PATTERN.matcher(uuid).matches();
    }

    /**
     * Convert a Tink UUID {@code String} object (07da44eeae8d448b8b67bdf5e0488832) to a Java {@code
     * UUID} object.
     *
     * @param uuid the Tink UUID string. Never null.
     * @return a read UUID object. Never null.
     */
    public static UUID fromTinkUUID(final String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(
                HEXADECIMAL_PATTERN.matcher(uuid).matches(), "Incorrect UUID: " + uuid);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uuid, 0, 8);
        stringBuilder.append('-');
        stringBuilder.append(uuid, 8, 12);
        stringBuilder.append('-');
        stringBuilder.append(uuid, 12, 16);
        stringBuilder.append('-');
        stringBuilder.append(uuid, 16, 20);
        stringBuilder.append('-');
        stringBuilder.append(uuid, 20, uuid.length());

        return UUID.fromString(stringBuilder.toString());
    }

    /**
     * Convert a Java {@code UUID} object to a Tink UUID {@code String} object
     * (07da44eeae8d448b8b67bdf5e0488832).
     */
    public static String toTinkUUID(final UUID uuid) {
        if (uuid == null) {
            return null;
        }

        // This code replicates the behavior of UUID.toString() without the splitting dashes.

        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        return (digits(mostSigBits >> 32, 8)
                + digits(mostSigBits >> 16, 4)
                + digits(mostSigBits, 4)
                + digits(leastSigBits >> 48, 4)
                + digits(leastSigBits, 12));
    }

    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    public static Date UUIDToDate(UUID uuid) {
        return new Date(UUIDs.unixTimestamp(uuid));
    }

    public static String generateUUID() {
        return toTinkUUID(UUID.randomUUID());
    }

    /**
     * Return the UUID from either a Tink UUID (07da44eeae8d448b8b67bdf5e0488832) or a UUID V4
     * (24e6b8cb-8e79-46cd-bbec-d354c4111aa5).
     */
    public static UUID fromString(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        return UUIDUtils.fromTinkUUID(value.replace("-", ""));
    }
}
