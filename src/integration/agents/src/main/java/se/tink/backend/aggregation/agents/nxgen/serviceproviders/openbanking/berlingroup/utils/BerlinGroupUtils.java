package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils;

import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

public final class BerlinGroupUtils {

    public static String generateCodeVerifier() {
        final SecureRandom sr = new SecureRandom();
        final byte[] code = new byte[43];
        sr.nextBytes(code);

        return Base64.getEncoder().withoutPadding().encodeToString(code);
    }

    public static String generateCodeChallenge(final String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(Hash.sha256(input));
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String generateSignature(final String input, final String signingKeyPath) {
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            RSA.signSha256(
                                    Pem.parsePrivateKey(
                                            Files.readAllBytes(Paths.get(signingKeyPath))),
                                    input.getBytes()));
        } catch (IOException e) {
            throw new IllegalStateException("Something went wrong when reading signingKeyPath.", e);
        }
    }

    public static String getFormattedCurrentDate(final String format, final String timeZone) {
        return formatDate(Calendar.getInstance().getTime(), format, timeZone);
    }

    public static String formatDate(final Date date, final String format, final String timeZone) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);

        if (!Strings.isNullOrEmpty(timeZone)) {
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        return sdf.format(date);
    }
}
