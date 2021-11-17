package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.libraries.cryptography.hash.Hash;

class DigestGenerator {

    static String generateDigest(String body) {
        return String.format(
                ChebancaConstants.HeaderValues.SHA_256.concat("%s"),
                Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }
}
