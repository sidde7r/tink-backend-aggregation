package se.tink.libraries.auth.encryption;

import com.lambdaworks.crypto.SCryptUtil;

public class ScryptWrapper implements HashingAlgorithmWrapper {
    @Override
    public boolean check(String cleartext, String hash) {
        return SCryptUtil.check(cleartext, hash);
    }

    @Override
    public String generate(String cleartext) {
        return SCryptUtil.scrypt(cleartext, 16384, 8, 1);
    }
}
