package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;

public final class SignatureUtil {

    private SignatureUtil() {}

    public static String generateDigest(String data) {
        return Signature.DIGEST_PREFIX + Base64.getEncoder().encodeToString(Hash.sha256(data));
    }

    public static String getSignatureHeaderValue(
            final String keyId,
            final String httpMethod,
            final String requestPath,
            final String date,
            final String digest,
            final String requestId,
            final EidasProxyConfiguration eidasProxyConf) {

        String signature =
                getSignatureValue(httpMethod, requestPath, date, digest, requestId, eidasProxyConf);

        return CmcicConstants.Signature.KEY_ID_NAME
                + "\""
                + keyId
                + "\","
                + CmcicConstants.Signature.ALGORITHM
                + ","
                + CmcicConstants.Signature.HEADERS
                + ","
                + CmcicConstants.Signature.SIGNATURE_NAME
                + "\""
                + signature
                + "\"";
    }

    private static String getSignatureValue(
            final String httpMethod,
            final String reqPath,
            final String date,
            final String digest,
            final String requestId,
            final EidasProxyConfiguration eidasProxyConf) {

        String signatureEntity =
                CmcicConstants.Signature.SIGNING_STRING
                        + httpMethod.toLowerCase()
                        + " "
                        + reqPath
                        + System.lineSeparator()
                        + CmcicConstants.Signature.DATE
                        + date
                        + System.lineSeparator()
                        + CmcicConstants.Signature.DIGEST
                        + digest
                        + System.lineSeparator()
                        + CmcicConstants.Signature.X_REQUEST_ID
                        + requestId
                        + System.lineSeparator()
                        + CmcicConstants.Signature.CONTENT_TYPE
                        + MediaType.APPLICATION_JSON;

        return new QsealcEidasProxySigner(eidasProxyConf, "Tink")
                .getSignatureBase64(signatureEntity.getBytes());
    }

    public static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }
}
