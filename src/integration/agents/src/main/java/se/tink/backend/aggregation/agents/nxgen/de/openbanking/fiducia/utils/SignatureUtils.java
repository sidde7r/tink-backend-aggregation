package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils;

import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.SignatureKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.SignatureValues;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SignatureUtils {
    public static String createSignature(
            PrivateKey privateKey,
            String keyId,
            String headers,
            String digest,
            String reqId,
            String date,
            String psuId) {
        String signature =
                SignatureKeys.KEY_ID
                        + keyId
                        + SignatureKeys.ALGORITHM
                        + SignatureValues.ALGORITHM
                        + SignatureKeys.HEADERS
                        + headers
                        + SignatureKeys.SIGNATURE;

        String signingString =
                SignatureKeys.X_REQUEST_ID
                        + reqId
                        + System.lineSeparator()
                        + SignatureKeys.DIGEST
                        + digest
                        + System.lineSeparator()
                        + SignatureKeys.DATE
                        + date;

        signingString +=
                Optional.ofNullable(psuId)
                        .map(psu -> System.lineSeparator() + SignatureKeys.PSU_ID + psu)
                        .orElse(SignatureKeys.EMPTY);
        return signature
                + Base64.getEncoder()
                        .encodeToString(RSA.signSha256(privateKey, signingString.getBytes()))
                + "\"";
    }

    public static String getCurrentDateFormatted() {
        return ThreadSafeDateFormat.FORMATTER_MINS_WITH_TIMEZONE.format(new Date());
    }

    public static String createDigest(String body) {
        return SignatureKeys.SHA_256 + Base64.getEncoder().encodeToString(Hash.sha256(body));
    }
}
