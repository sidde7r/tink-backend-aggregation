package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public class JWETest {
    private KeyPair keyPair = RSA.generateKeyPair();
    private static final String DATA = "data";
    private static final String KEY = "dataKey";
    private static final String KEY_ID = "keyId";

    @Test
    @SneakyThrows
    public void jWEBuilderBuildShouldReturnJWE() {
        // given
        JWTClaimsSet claims = new JWTClaimsSet.Builder().claim(KEY, DATA).build();
        String jweEncrypted =
                new JWE.Builder()
                        .setJWEHeader()
                        .setJwtClaimsSet(claims)
                        .setJWEHeader()
                        .setRSAEnrypter((RSAPublicKey) keyPair.getPublic())
                        .build();
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        // when
        jweDecrypted.decrypt(new RSADecrypter(keyPair.getPrivate()));
        // then
        assertThat(jweDecrypted.getPayload().toJSONObject().getAsString(KEY)).isEqualTo(DATA);
        assertThat(jweDecrypted.getHeader().getKeyID()).isNull();
    }

    @Test
    @SneakyThrows
    public void settingKIDtoHeaderInJWEBuilderShouldReturnJWEWithKID() {
        // given
        JWTClaimsSet claims = new JWTClaimsSet.Builder().claim(KEY, DATA).build();
        String jweEncrypted =
                new JWE.Builder()
                        .setJWEHeaderWithKeyId(KEY_ID)
                        .setJwtClaimsSet(claims)
                        .setRSAEnrypter((RSAPublicKey) keyPair.getPublic())
                        .build();
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        // when
        jweDecrypted.decrypt(new RSADecrypter(keyPair.getPrivate()));
        // then
        assertThat(jweDecrypted.getHeader().getKeyID()).isEqualTo(KEY_ID);
    }
}
