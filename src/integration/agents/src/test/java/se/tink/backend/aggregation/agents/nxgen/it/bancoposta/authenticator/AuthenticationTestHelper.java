package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jose.JWEObject;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AuthenticationTestHelper {

    public static void assertDefaultJWEValues(JWEObject jweObject) {
        assertThat(jweObject.getHeader().getKeyID()).isEqualTo(AuthenticationTestData.APP_ID);
        assertThat(jweObject.getPayload().toJSONObject().getAsString("otp-specs")).isNotEmpty();
        String payload = jweObject.getPayload().toJSONObject().toJSONString();
        Map<String, String> payloadMap =
                SerializationUtils.deserializeFromString(payload, Map.class);
        assertThat(payloadMap)
                .containsKeys("otp-specs", "kid-sha256", "iat", "exp", "iss", "jti", "nbf");
    }

    public static BancoPostaStorage prepareStorageForTests() {
        KeyPair keyPair = RSA.generateKeyPair();
        BancoPostaStorage storage = Mockito.mock(BancoPostaStorage.class);
        given(storage.getKeyPair()).willReturn(keyPair);
        given(storage.getAccountNumber()).willReturn("accountNumber");
        given(storage.getUserPin()).willReturn("userPin");
        given(storage.getOtpSecretKey())
                .willReturn(BaseEncoding.base32().decode(AuthenticationTestData.OTP_SECRET_KEY));
        given(storage.getAppId()).willReturn(AuthenticationTestData.APP_ID);
        given(storage.getPubServerKey()).willReturn((RSAPublicKey) keyPair.getPublic());
        given(storage.getRegisterToken()).willReturn("registerToken");
        given(storage.getAppRegisterId()).willReturn("appRegisterId");
        return storage;
    }
}
