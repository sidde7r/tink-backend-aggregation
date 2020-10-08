package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants.KeyGenerator;

public class OpAuthenticationTokenGenerator {

    public static String calculateAuthToken(String seed) {
        try {
            String salt = KeyGenerator.SALT;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(Hex.decodeHex((seed + salt).toCharArray()));
            return OpBankConstants.AUTH_TOKEN_PREFIX + String.valueOf(Hex.encodeHex(hash));
        } catch (DecoderException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
