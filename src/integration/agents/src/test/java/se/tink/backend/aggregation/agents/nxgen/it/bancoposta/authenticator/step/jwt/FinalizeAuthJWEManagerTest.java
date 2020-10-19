package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.AuthenticationTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.FinalizeAuthJWEManager;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FinalizeAuthJWEManagerTest {
    private FinalizeAuthJWEManager objUnderTest;
    private BancoPostaStorage storage;
    private static final String APP_REGISTER_ID = "appRegisterID";
    private static final String TRANSACTION_CHALLENGE =
            "HVCXr0hkTDgXXlO2gkewlYLy8I1SpyZrLmGe18RZnCs";
    private static final String RAND_K = "-2869187136524957696";
    private static final String USER_PIN = "userPIN";

    @Before
    public void init() {
        this.storage = AuthenticationTestHelper.prepareStorageForTests();
        this.objUnderTest = new FinalizeAuthJWEManager(storage);
    }

    @SneakyThrows
    @Test
    public void genCheckRegisterWEShouldReturnJWE() {
        // given
        // when
        String jweEncrypted = objUnderTest.genCheckRegisterJWE();
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        AuthenticationTestHelper.assertDefaultJWEValues(jweDecrypted);
        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();

        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(1).containsKeys(APP_REGISTER_ID);
    }

    @SneakyThrows
    @Test
    public void genChallengeJWEShouldReturnJWE() {
        // given
        // when
        String jweEncrypted = objUnderTest.genChallengeJWE();
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        AuthenticationTestHelper.assertDefaultJWEValues(jweDecrypted);

        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();
        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(1).containsKeys(APP_REGISTER_ID);
    }

    @SneakyThrows
    @Test
    public void genAuthorizeTransactionJWEShouldReturnJWE() {
        // given
        ChallengeResponse challengeResponse =
                new ChallengeResponse(UUID.randomUUID().toString(), TRANSACTION_CHALLENGE, RAND_K);
        // when
        String jweEncrypted = objUnderTest.genAuthorizeTransactionJWE(challengeResponse, USER_PIN);
        JWEObject jweDecrypted = JWEObject.parse(jweEncrypted);
        jweDecrypted.decrypt(new RSADecrypter(storage.getKeyPair().getPrivate()));
        // then
        AuthenticationTestHelper.assertDefaultJWEValues(jweDecrypted);

        String dataClaim = jweDecrypted.getPayload().toJSONObject().getAsString("data");
        assertThat(dataClaim).isNotEmpty();
        Map<String, String> data = SerializationUtils.deserializeFromString(dataClaim, Map.class);
        assertThat(data).hasSize(7);
    }
}
