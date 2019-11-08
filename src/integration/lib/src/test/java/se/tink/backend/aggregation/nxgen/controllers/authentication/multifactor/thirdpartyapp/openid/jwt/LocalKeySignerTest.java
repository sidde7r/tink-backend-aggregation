package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import java.text.ParseException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class LocalKeySignerTest {

    private static final String TEST_SIGNING_PRIVATE_KEY =
            "MIIEowIBAAKCAQEA1UHi5qoIsGZyjj/Ks20szf4QZHLcx+sSjBkzX0EHKMQc4GPwkEfOZFHdFwK+NSOP2A5ju1XTa/Mexcijg8ahAhpB5MPrbAbqr1guJhAWdiys7WouzIxOVAszvk3NxEFs5cfPdmLm+E4GfxlS/nvoaBH1uZ5NeYxvdyq+dKCP787DEPlUBLnIcFheQV5wZ2I6PqncPWCwk97luezzC57WqpzKa0F/S+RHx1fknv8h0kCZVKJU/O6jbwpa97mv5lNU34tI//t13jmJyoGtZ4qcSWvoXqbZZIMyDJFFTLpPXAYy7YZTiaQP3KfzVRPKw07867tVxfrxgULdTdVNmQtVZwIDAQABAoIBAFh5619zsAMvndtyj/B9HRehjr9+ACdZWeqSEgQ1AHljU++sxwO1JuXsKOQBGVUGmgkzfyEb7Ile5qd8JIrpf6dVqXz149h/ziJNXWghSJBi3KPrZ9spYt3vf9o2gWYpFrsGeQZO5jCifoOAyDQYyEVVHjOSf+Yr45r3Ouk8LDt7NO7JfTuaPjUbkvjfc/ebHg0gna05byaYotqNA+r2E3G99TMV7rGaqieOoEBdZ2KrSR0XIVpx87AOSoVXU0RbrIVu2VEfLIOZKNAbA/jt2t+vPbgBa3ZieU1p6+5lgzMuB+UJ1xf3NwmreyqqapuZiZ0Bmh/jpTnY2gxMf4IZ1VECgYEA8OQ2hYxJ/H3m3tW8K5oy2vnqqW0GWTnBPfpsuF4itglc601VSc3qHeJsw2JIZO36lu6N2i2auWp12VHSdVaAfcxtHm0YqbH7RRBMGKe6eiOwCommw7flTFbaFIpKCs3dOwK1iTqR6AZHrIx8kJfdGSU5vsvrgDqxNN3qNwu/6ZMCgYEA4qH5/5a9ZhSVw7IJc6RGbIEJI1sHWhUHZyEs+bGqUJLs1tcqgI39N2mSWJbglNuC0KBW0LMWsfDTwKOuxPMlNw/Y5p1tJffNsjbszPVsySi1Pmum7W95k4joCva8ZzJjPRJJZegfNumKZHCs3skN7SEIFcRbqFwI8yoXAKV/eV0CgYAlFl22jfTOJOwonquL4v+1awjP8PMrRZMU4btfX07ky+g8afmZlVRYKYYpRrZyo5kTJ8M7Ng4+Qb/HJ6vJWPoLZC/goVdMuRRAe9Pbb+dr6pat6Kd+No/dAvosc2YW578J2M7uQ9A28tCSJkb/VAI8XtjXITou0dp6kzf+JZb0dQKBgQCFomU7pLMBolm9Dxorqk4sZNnykxZ+s6+tzA4tHeoDoN8uv6k8LH2HUyUMP8sle1pjkmgen7teKVdzXBEN6SGkmh+XvHRD0x7jWye+o2kGJI7aw7emgfj6WdwZuuvVHg0OUd4dzQW653LHTvlgVMV1cejGjQZO/BX67HKr2uqTsQKBgGFNSdZ4GjJGM3R6BmDgSbo1HelOVYDjPomTX46U5Qg0GcU9lpzLMq3EcIPi97uvp5RUouiZXwOxZpiwDXjSXcBoPhAz94gLLg0Uzgjs0jspa304PO9elkqh1fNMjzJcbSRauuMibkQpbUBnGNM/7igls4cSy22/Afnm7sk08Xi2";
    private static final String TEST_SIGNING_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1UHi5qoIsGZyjj/Ks20szf4QZHLcx+sSjBkzX0EHKMQc4GPwkEfOZFHdFwK+NSOP2A5ju1XTa/Mexcijg8ahAhpB5MPrbAbqr1guJhAWdiys7WouzIxOVAszvk3NxEFs5cfPdmLm+E4GfxlS/nvoaBH1uZ5NeYxvdyq+dKCP787DEPlUBLnIcFheQV5wZ2I6PqncPWCwk97luezzC57WqpzKa0F/S+RHx1fknv8h0kCZVKJU/O6jbwpa97mv5lNU34tI//t13jmJyoGtZ4qcSWvoXqbZZIMyDJFFTLpPXAYy7YZTiaQP3KfzVRPKw07867tVxfrxgULdTdVNmQtVZwIDAQAB";

    private JwtSignerTestHelper jwtSignerTestHelper;

    @Before
    public void setup() {
        JwtSigner signer =
                new LocalKeySigner(
                        "keyIdValue",
                        RSA.getPrivateKeyFromBytes(
                                EncodingUtils.decodeBase64String(TEST_SIGNING_PRIVATE_KEY)));

        RSASSAVerifier verifier =
                new RSASSAVerifier(
                        RSA.getPubKeyFromBytes(
                                EncodingUtils.decodeBase64String(TEST_SIGNING_PUBLIC_KEY)));

        jwtSignerTestHelper = new JwtSignerTestHelper(signer, verifier);
    }

    @Test
    public void ensure_keyId_isAddedBySigner() throws ParseException {

        jwtSignerTestHelper.ensure_keyId_isAddedBySigner("keyIdValue");
    }

    @Test
    public void ensure_algorithm_isAddedBySigner() throws ParseException {

        jwtSignerTestHelper.ensure_algorithm_isAddedBySigner();
    }

    @Test
    public void ensure_PS256_yieldsValidSignature() throws ParseException, JOSEException {

        jwtSignerTestHelper.ensure_PS256_yieldsValidSignature();
    }

    @Test
    public void ensure_RS256_yieldsValidSignature() throws ParseException, JOSEException {

        jwtSignerTestHelper.ensure_RS256_yieldsValidSignature();
    }
}
