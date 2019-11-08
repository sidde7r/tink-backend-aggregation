package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import java.text.ParseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.EidasJwtSigner.EidasSigningKey;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner.Algorithm;

@Ignore
public class EidasJwtKeySignerTest {

    private static final String TEST_SIGNING_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3X1vjSsIoLvPUQKnNDz1OZydo5qdgTLImmLpVfLVRzecs/sV3DHSkbRRAKAG+6ISDxbUAAR1Zna9Eun1BlPrNz6MbV13Vsf3H4EAWIXkjyZwY3GAUDFC0prz2Rcw5lnXP7Q56iElq22f+PjNBe1SOD0TQBCAP8fAkrZMd+aBnAicKrlxgpmdTGPyxyZEACypUMWoNs/MyI4eXw/xL0o2USCIDgsjr8MCWCeiLlRcgRi90oEWVONU4YPyQ0rhyH0OM7qfoS1VhpZXleGBhWbRBvFxd0W+RQ7i+iek4so/qWLLxnXQNZ5qaLYpBVqXre+Dh0SIKlgkxFyJqzZH48/KmwIDAQAB";

    private JwtSignerTestHelper jwtSignerTestHelper;

    @Before
    public void setup() {
        JwtSigner signer =
                new EidasJwtSigner(
                        ImmutableMap.<Algorithm, EidasSigningKey>builder()
                                .put(
                                        Algorithm.PS256,
                                        EidasSigningKey.of(
                                                "PSDSE-FINA-44059", QsealcAlg.EIDAS_PSS_SHA256))
                                .put(
                                        Algorithm.RS256,
                                        EidasSigningKey.of(
                                                "PSDSE-FINA-44059-RSA", QsealcAlg.EIDAS_RSA_SHA256))
                                .build());

        RSASSAVerifier verifier =
                new RSASSAVerifier(
                        RSA.getPubKeyFromBytes(
                                EncodingUtils.decodeBase64String(TEST_SIGNING_PUBLIC_KEY)));

        jwtSignerTestHelper = new JwtSignerTestHelper(signer, verifier);
    }

    @Test
    public void ensure_keyId_isAddedBySigner() throws ParseException {

        jwtSignerTestHelper.ensure_keyId_isAddedBySigner("PSDSE-FINA-44059");
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
