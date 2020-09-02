package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.nimbusds.jose.JWEObject;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.RegisterAppJWEManager;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterAppJWEManagerTest extends JWEManagerTest {
    private RegisterAppJWEManager objUnderTest;
    private static final String REGISTER_TOKEN = "registerToken";
    private static final String USER_PIN = "userPIN";
    private static final String IDP_ACCESS_TOKEN = "idpAccessToken";

    @Before
    public void init() {
        initSetup();
        this.objUnderTest = new RegisterAppJWEManager(userContext);
        given(userContext.getRegisterToken()).willReturn(REGISTER_TOKEN);
        given(userContext.getUserPin()).willReturn(USER_PIN);
    }

    @SneakyThrows
    @Test
    public void genActivationJWEShouldReturnJWEWithUserPinIfUserPinSetIsRequired() {
        // given
        given(userContext.isUserPinSetRequired()).willReturn(true);
        // when
        String jweEncrypted = objUnderTest.genRegisterAppJWE();
        JWEObject jweDecrypted = getDecryptedJwe(jweEncrypted);
        // then
        assertDefaultJWEValues(jweDecrypted);
        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();

        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(3).containsKeys(REGISTER_TOKEN, IDP_ACCESS_TOKEN, USER_PIN);
    }

    @SneakyThrows
    @Test
    public void genActivationJWEShouldReturnJWEWithoutUserPinIfUserPinSetIsNotRequired() {
        // given
        given(userContext.isUserPinSetRequired()).willReturn(false);
        // when
        String jweEncrypted = objUnderTest.genRegisterAppJWE();
        JWEObject jweDecrypted = getDecryptedJwe(jweEncrypted);
        // then
        assertDefaultJWEValues(jweDecrypted);
        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();

        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(2).containsKeys(REGISTER_TOKEN, IDP_ACCESS_TOKEN);
    }
}
