package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

public final class IngBaseUtils {

    private IngBaseUtils() {}

    public static String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String calculateDigest(String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String getFormattedCurrentDate(String format, String timeZone) {
        return formatDate(Calendar.getInstance().getTime(), format, timeZone);
    }

    private static String formatDate(Date date, String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        if (!Strings.isNullOrEmpty(timeZone)) {
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        return sdf.format(date);
    }

    public static String getCertificateSerial(String encodedCertificate)
            throws CertificateException {
        final X509Certificate certificate =
                (X509Certificate)
                        Pem.parseCertificate(Base64.getDecoder().decode(encodedCertificate));
        return String.format("SN=%x", certificate.getSerialNumber());
    }
}
