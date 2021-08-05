package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class CmcicDigestProvider {

    public String generateDigest(String data) {
        return CmcicConstants.Signature.DIGEST_PREFIX
                + Base64.getEncoder()
                        .encodeToString(Hash.sha256(data.getBytes(StandardCharsets.UTF_8)));
    }
}
