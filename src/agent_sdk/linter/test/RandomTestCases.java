package src.agent_sdk.linter.test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class RandomTestCases {
    public void preventRandom() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        new Random();
    }

    public void preventSecureRandom() throws NoSuchAlgorithmException {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        new SecureRandom();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        SecureRandom.getSeed(4);

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        SecureRandom.getInstanceStrong();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        SecureRandom.getInstance(null);
    }
}
