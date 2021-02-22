package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner.Algorithm;

@Ignore
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtSignerTestHelper {

    private final JwtSigner signer;
    private final JWSVerifier rs256Verifier;
    private final JWSVerifier ps256Verifier;

    JwtSignerTestHelper(JwtSigner signer, JWSVerifier verifier) {
        this(signer, verifier, verifier);
    }

    void ensure_keyId_isAddedBySigner() throws ParseException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertEquals(signedJwt.getHeader().getKeyID(), "PSDSE-FINA-44059");
    }

    void ensure_algorithm_isAddedBySigner() throws ParseException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertEquals(signedJwt.getHeader().getAlgorithm(), JWSAlgorithm.PS256);
    }

    void ensure_PS256_yieldsValidSignature() throws ParseException, JOSEException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.PS256, Collections.emptyMap(), payloadClaims, false);

        System.out.println(resultJwt);
        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertTrue(signedJwt.verify(ps256Verifier));
    }

    void ensure_RS256_yieldsValidSignature() throws ParseException, JOSEException {

        ImmutableMap<String, Object> payloadClaims = newExamplePayloadBuilder().build();

        String resultJwt =
                signer.sign(Algorithm.RS256, Collections.emptyMap(), payloadClaims, false);

        System.out.println(resultJwt);
        SignedJWT signedJwt = SignedJWT.parse(resultJwt);

        Assert.assertTrue(signedJwt.verify(rs256Verifier));
    }

    private ImmutableMap.Builder<String, Object> newExamplePayloadBuilder() {
        return ImmutableMap.<String, Object>builder()
                .put(OpenIdConstants.Params.RESPONSE_TYPE, "responseTypeValue")
                .put(OpenIdConstants.Params.CLIENT_ID, "clientIdValue")
                .put(OpenIdConstants.Params.REDIRECT_URI, "redirectValue")
                .put(OpenIdConstants.Ps256.PayloadClaims.ISSUER, "issuerValue");
    }
}
