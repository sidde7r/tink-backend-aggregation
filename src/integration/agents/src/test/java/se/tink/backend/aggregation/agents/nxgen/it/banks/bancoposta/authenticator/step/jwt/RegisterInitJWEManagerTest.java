package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.AuthenticationTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterInitJWEManagerTest {
    private RegisterInitJWEManager objUnderTest;
    private BancoPostaStorage storage;
    private static final String INIT_CODE_CHALLENGE = "initCodeChallenge";

    @Before
    public void init() {
        this.storage = AuthenticationTestHelper.prepareStorageForTests();
        this.objUnderTest = new RegisterInitJWEManager(storage);
    }

    @SneakyThrows
    @Test
    public void genActivationJWEShouldReturnJWE() {
        // given
        // when
        String jweEncrypted = objUnderTest.genActivationJWE();
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        AuthenticationTestHelper.assertDefaultJWEValues(jweDecrypted);
        assertThat(jweDecrypted.getPayload().toJSONObject().getAsString("sub"))
                .isEqualTo("activation");
    }

    @SneakyThrows
    @Test
    public void genRegisterJWEShouldReturnJWE() {
        // given
        // when
        String jweEncrypted = objUnderTest.genRegisterJWE(INIT_CODE_CHALLENGE);
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        assertThat(jweDecrypted.getPayload().toJSONObject().getAsString("sub"))
                .isEqualTo("register");
        assertThat(jweDecrypted.getHeader().getKeyID()).isNull();
        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();

        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(3);
    }
}
