package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JWTUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public final class IngUtils {

    private IngUtils() {}

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String getFormattedCurrentDate(String format, String timeZone) {
        return formatDate(Calendar.getInstance().getTime(), format, timeZone);
    }

    public static String generateSignature(String input, String signingKeyPath, String algorithm) {
        return Base64.getEncoder()
                .encodeToString(
                        RSA.signSha256(
                                JWTUtils.readSigningKey(signingKeyPath, algorithm),
                                input.getBytes()));
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static String formatDate(Date date, String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        if (!Strings.isNullOrEmpty(timeZone)) {
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        return sdf.format(date);
    }
}
