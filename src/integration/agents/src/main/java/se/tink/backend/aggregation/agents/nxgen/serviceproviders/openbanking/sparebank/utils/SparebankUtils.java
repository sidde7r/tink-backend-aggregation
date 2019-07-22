package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils;

import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public class SparebankUtils {

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }
}
