package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.utils;

import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;

public class SignatureUtils {

    //create a createSignature method??
    public static String createSignature(QsealcEidasProxySigner qsealSigner,String consentSignatureString, String keyId) {
        String signedB64Signature =
            qsealSigner.getSignatureBase64(consentSignatureString.getBytes());
        return String.format(
            "keyId=\"%s\", algorithm=\"rsa-sha256\", headers=\"x-request-id tpp-redirect-uri digest\", signature=\"%s\"", keyId, signedB64Signature);
    }
    public static String createSignatureParameters(String uuid, String digest, String redirectUri) {
        String consentSignatureString =
            String.format(
                HeaderKeys.X_REQUEST_ID + ": %s\n" + HeaderKeys.TPP_REDIRECT_URI + ": %s\n" + HeaderKeys.DIGEST + ": %s",
                uuid,
                redirectUri,
                digest);
        return consentSignatureString;
    }

    public static String createDigest(String body) {
        return "SHA-256=" + Base64.getEncoder().encodeToString(Hash.sha256(body));

    }


}
