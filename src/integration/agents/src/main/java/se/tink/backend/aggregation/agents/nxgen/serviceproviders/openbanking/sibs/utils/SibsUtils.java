package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import com.google.common.base.Strings;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SignatureValues;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.eidas.EidasProxyConstants.CertificateId;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private SibsUtils() {}

    public static String getSigningString(
            String digest, String transactionId, String requestId, String signatureStringDate) {

        StringBuilder signingString = new StringBuilder();

        if (!Strings.isNullOrEmpty(digest)) {
            signingString
                    .append(HeaderKeys.DIGEST.toLowerCase())
                    .append(COLON_SPACE)
                    .append(HeaderValues.DIGEST_PREFIX)
                    .append(digest)
                    .append(NEW_LINE);
        }

        signingString
                .append(HeaderKeys.TPP_TRANSACTION_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(transactionId)
                .append(NEW_LINE)
                .append(HeaderKeys.TPP_REQUEST_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(requestId)
                .append(NEW_LINE)
                .append(HeaderKeys.DATE.toLowerCase())
                .append(COLON_SPACE)
                .append(signatureStringDate);

        return signingString.toString();
    }

    public static String getSignature(
            String digest,
            String transactionId,
            String requestId,
            String signatureStringDate,
            URL eidasProxyBaseUrl,
            String clientSigningCertificateSerialNumber) {

        String toSignString =
                getSigningString(digest, transactionId, requestId, signatureStringDate);

        final QsealcEidasProxySigner proxySigner =
                new QsealcEidasProxySigner(eidasProxyBaseUrl, CertificateId.TINK.toString());
        String signatureBase64Sha = proxySigner.getSignatureBase64(toSignString.getBytes());

        return formSignature(digest, clientSigningCertificateSerialNumber, signatureBase64Sha);
    }

    public static String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }

    public static String getRequestId() {
        return UUID.randomUUID().toString().replace(DASH, StringUtils.EMPTY);
    }

    private static String formSignature(
            String digest, String clientSigningCertificateSerialNumber, String signatureBase64Sha) {
        return String.format(
                Formats.SIGNATURE_STRING_FORMAT,
                clientSigningCertificateSerialNumber,
                SignatureValues.RSA_SHA256,
                Strings.isNullOrEmpty(digest)
                        ? SignatureValues.HEADERS_NO_DIGEST
                        : SignatureValues.HEADERS,
                signatureBase64Sha);
    }
}
