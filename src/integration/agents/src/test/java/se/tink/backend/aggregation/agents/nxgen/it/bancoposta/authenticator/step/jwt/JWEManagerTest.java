package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.SneakyThrows;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.AuthenticationTestData;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class JWEManagerTest {
    protected UserContext userContext;
    protected KeyPair keyPair = RSA.generateKeyPair();

    protected void initSetup() {
        this.userContext = Mockito.mock(UserContext.class);
        given(userContext.getPubServerKey()).willReturn((RSAPublicKey) keyPair.getPublic());
        given(userContext.getAppId()).willReturn(AuthenticationTestData.APP_ID);
        given(userContext.getOtpSecretKey())
                .willReturn(BaseEncoding.base32().decode(AuthenticationTestData.OTP_SECRET_KEY));
    }

    protected void assertDefaultJWEValues(JWEObject jweObject) {
        assertThat(jweObject.getHeader().getKeyID()).isEqualTo(AuthenticationTestData.APP_ID);
        assertThat(jweObject.getPayload().toJSONObject().getAsString("otp-specs")).isNotEmpty();
        String payload = jweObject.getPayload().toJSONObject().toJSONString();
        Map<String, String> payloadMap =
                SerializationUtils.deserializeFromString(payload, Map.class);
        assertThat(payloadMap)
                .containsKeys("otp-specs", "kid-sha256", "iat", "exp", "iss", "jti", "nbf");
    }

    @SneakyThrows
    protected JWEObject getDecryptedJwe(String jweEncrypted) {
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(keyPair.getPrivate()));
        return jweDecrypted;
    }
}
