package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import java.nio.charset.StandardCharsets;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditAgricoleSignatureUtils {

  public static String getDigest(Object body) {
    byte[] bytes =
        SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
    return Hash.sha256Base64(bytes);
  }

}
