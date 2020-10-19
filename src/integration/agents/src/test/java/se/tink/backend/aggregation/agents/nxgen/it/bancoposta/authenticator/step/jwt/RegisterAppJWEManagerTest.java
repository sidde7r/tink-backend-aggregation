package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.AuthenticationTestHelper.assertDefaultJWEValues;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.AuthenticationTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.RegisterAppJWEManager;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterAppJWEManagerTest {
    private RegisterAppJWEManager objUnderTest;
    private BancoPostaStorage storage;
    private static final String REGISTER_TOKEN = "registerToken";
    private static final String USER_PIN = "userPIN";
    private static final String IDP_ACCESS_TOKEN = "idpAccessToken";

    @Before
    public void init() {
        this.storage = AuthenticationTestHelper.prepareStorageForTests();
        this.objUnderTest = new RegisterAppJWEManager(storage);
    }

    @SneakyThrows
    @Test
    public void genActivationJWEShouldReturnJWEWithUserPinIfUserPinSetIsRequired() {
        // given
        given(storage.isUserPinSetRequired()).willReturn(true);
        // when
        String jweEncrypted = objUnderTest.genRegisterAppJWE(USER_PIN);
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
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
        given(storage.isUserPinSetRequired()).willReturn(false);
        // when
        String jweEncrypted = objUnderTest.genRegisterAppJWE(USER_PIN);
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        assertDefaultJWEValues(jweDecrypted);
        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();

        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(2).containsKeys(REGISTER_TOKEN, IDP_ACCESS_TOKEN);
    }
}
