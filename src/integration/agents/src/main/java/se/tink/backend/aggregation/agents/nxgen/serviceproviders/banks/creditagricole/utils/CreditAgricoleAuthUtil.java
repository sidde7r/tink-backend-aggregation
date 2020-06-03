package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.utils;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class CreditAgricoleAuthUtil {

    public static String createEncryptedAccountCode(
            String mappedAccountCode, RSAPublicKey publicKey) {
        byte[] encryptedAccountCode = RSA.encryptNonePkcs1(publicKey, mappedAccountCode.getBytes());
        return EncodingUtils.encodeAsBase64String(encryptedAccountCode);
    }

    public static String mapAccountCodeToNumpadSequence(
            String numpadSequence, String realAccountCode) {
        String numpadSequenceWithoutDelimiter = numpadSequence.replace(";", "");
        return Arrays.stream(realAccountCode.split(""))
                .map(numpadSequenceWithoutDelimiter::indexOf)
                .map(String::valueOf)
                .collect(Collectors.joining(";"));
    }

    public static RSAPublicKey getPublicKey(String publicKeyAsBase64) {
        return RSA.getPubKeyFromBytes(EncodingUtils.decodeBase64String(publicKeyAsBase64));
    }
}
