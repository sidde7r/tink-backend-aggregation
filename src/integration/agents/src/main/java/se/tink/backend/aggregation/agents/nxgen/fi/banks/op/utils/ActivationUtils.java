package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils.srp.ClientEvidenceMessageResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;

public class ActivationUtils {
    private static byte[] encryptionKeyFromSrpSessionKey(
            ClientEvidenceMessageResponse clientEvidenceMessageResponse) {
        byte[] srpSessionKey = clientEvidenceMessageResponse.getSessionKeyAsBytes();
        byte[] hashOfSrpSessionKey = Hash.sha256(srpSessionKey);
        byte[] hmacData =
                EncodingUtils.decodeHexString(OpBankConstants.Activation.ACTIVATION_HMAC_DATA);
        return Hash.hmacSha256(hashOfSrpSessionKey, hmacData);
    }

    // This method is used to decrypt `activationMessage1` and `staticVector`
    public static byte[] decryptActivationData(
            ClientEvidenceMessageResponse clientEvidenceMessageResponse,
            String encryptedDataAsHex,
            String counterNonceAsHex) {
        byte[] key = encryptionKeyFromSrpSessionKey(clientEvidenceMessageResponse);
        byte[] encryptedData = EncodingUtils.decodeHexString(encryptedDataAsHex);
        byte[] counterNonce = EncodingUtils.decodeHexString(counterNonceAsHex);
        return AES.decryptCtr(key, counterNonce, encryptedData);
    }
}
