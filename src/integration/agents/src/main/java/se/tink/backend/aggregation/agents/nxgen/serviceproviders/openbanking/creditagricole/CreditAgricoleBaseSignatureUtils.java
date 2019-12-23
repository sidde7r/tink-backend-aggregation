package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import java.nio.charset.StandardCharsets;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditAgricoleBaseSignatureUtils {

    public static String getDigest(Object body) {
        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        return Hash.sha256Base64(bytes);
    }
}
