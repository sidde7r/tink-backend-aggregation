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
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

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
            final EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {

        String signature =
                getSignatureValue(
                        httpMethod,
                        requestPath,
                        date,
                        digest,
                        requestId,
                        eidasProxyConf,
                        eidasIdentity);

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
            final EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity) {

        String signatureEntity =
                CmcicConstants.Signature.SIGNING_STRING
                        + httpMethod.toLowerCase()
                        + " "
                        + reqPath
                        + "\n"
                        + CmcicConstants.Signature.DATE
                        + date
                        + "\n"
                        + CmcicConstants.Signature.DIGEST
                        + digest
                        + "\n"
                        + CmcicConstants.Signature.X_REQUEST_ID
                        + requestId
                        + "\n"
                        + CmcicConstants.Signature.CONTENT_TYPE
                        + MediaType.APPLICATION_JSON;

        return QsealcSigner.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        "Tink")
                .getSignatureBase64(signatureEntity.getBytes());
    }

    public static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }
}
