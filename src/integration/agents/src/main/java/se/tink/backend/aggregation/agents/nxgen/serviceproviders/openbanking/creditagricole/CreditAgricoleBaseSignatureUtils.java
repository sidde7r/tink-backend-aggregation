package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.nio.charset.StandardCharsets;

public class CreditAgricoleBaseSignatureUtils {

  public static String getDigest(Object body) {
    byte[] bytes =
        SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
    return Hash.sha256Base64(bytes);
  }

}
