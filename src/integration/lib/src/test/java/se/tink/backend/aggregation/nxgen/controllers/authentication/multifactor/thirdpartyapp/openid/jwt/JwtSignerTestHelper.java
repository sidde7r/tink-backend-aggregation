package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Collections;
import org.junit.Assert;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PS256.PAYLOAD_CLAIMS;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtSigner.Algorithm;

public class JwtSignerTestHelper {

    private final JwtSigner signer;
    private final JWSVerifier rs256Verifier;
    private final JWSVerifier ps256Verifier;

    public JwtSignerTestHelper(JwtSigner signer, JWSVerifier verifier) {
        this(signer, verifier, verifier);
    }

    public JwtSignerTestHelper(
            JwtSigner signer, JWSVerifier rs256Verifier, JWSVerifier ps256Verifier) {
        this.signer = signer;
        this.rs256Verifier = rs256Verifier;
        this.ps256Verifier = ps256Verifier;
    }

    public void ensure_keyId_isAddedBySigner(String expectedKeyId) throws ParseException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertEquals(signedJwt.getHeader().getKeyID(), expectedKeyId);
    }

    public void ensure_algorithm_isAddedBySigner() throws ParseException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertEquals(signedJwt.getHeader().getAlgorithm(), JWSAlgorithm.PS256);
    }

    public void ensure_PS256_yieldsValidSignature() throws ParseException, JOSEException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        System.out.println(resultJwt);
        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertTrue(signedJwt.verify(ps256Verifier));
    }

    public void ensure_RS256_yieldsValidSignature() throws ParseException, JOSEException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.RS256, Collections.emptyMap(), payloadClaims, false);

        System.out.println(resultJwt);
        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertTrue(signedJwt.verify(rs256Verifier));
    }

    private ImmutableMap.Builder<String, Object> newExamplePayloadBuilder() {
        return ImmutableMap.<String, Object>builder()
                .put(Params.RESPONSE_TYPE, "responseTypeValue")
                .put(Params.CLIENT_ID, "clientIdValue")
                .put(Params.REDIRECT_URI, "redirectValue")
                .put(PAYLOAD_CLAIMS.ISSUER, "issuerValue");
    }
}
