package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.nimbusds.jose.JWEObject;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.RegisterInitJWEManager;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterInitJWEManagerTest extends JWEManagerTest {
    private RegisterInitJWEManager objUnderTest;
    private static final String INIT_CODE_CHALLENGE = "initCodeChallenge";

    @Before
    public void init() {
        initSetup();
        this.objUnderTest = new RegisterInitJWEManager(userContext);
    }

    @SneakyThrows
    @Test
    public void genActivationJWEShouldReturnJWE() {
        // given
        // when
        String jweEncrypted = objUnderTest.genActivationJWE();
        JWEObject jweDecrypted = getDecryptedJwe(jweEncrypted);
        // then
        assertDefaultJWEValues(jweDecrypted);
        assertThat(jweDecrypted.getPayload().toJSONObject().getAsString("sub"))
                .isEqualTo("activation");
    }

    @SneakyThrows
    @Test
    public void genRegisterJWEShouldReturnJWE() {
        // given
        given(userContext.getKeyPair()).willReturn(keyPair);
        // when
        String jweEncrypted = objUnderTest.genRegisterJWE(INIT_CODE_CHALLENGE);
        JWEObject jweDecrypted = getDecryptedJwe(jweEncrypted);
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
