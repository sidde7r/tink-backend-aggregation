package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils;

import com.google.common.primitives.Bytes;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class AuthTokenUtils {
    public static String calculateAuthToken(String seedAsHex) {
        byte[] authTokenKey = EncodingUtils.decodeHexString(OpBankConstants.AUTH_TOKEN_KEY);
        byte[] seed = EncodingUtils.decodeHexString(seedAsHex);
        byte[] digest = Hash.sha1(Bytes.concat(seed, authTokenKey));
        return OpBankConstants.AUTH_TOKEN_START + EncodingUtils.encodeHexAsString(digest);
    }
}
