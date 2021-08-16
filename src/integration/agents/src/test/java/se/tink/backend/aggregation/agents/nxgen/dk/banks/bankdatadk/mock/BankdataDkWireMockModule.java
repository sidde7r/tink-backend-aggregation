package se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockTest.RESOURCES_BASE_DIR;

import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.module.loader.TestModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoComponentsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelperState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataCryptoHelperStateGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util.NemIdWireMockIframeControllerInitializer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Ignore
@RequiredArgsConstructor
public final class BankdataDkWireMockModule extends TestModule {

    /*
    User credentials
     */
    static final String SAMPLE_USER_ID = "SAMPLE_USER_ID";
    static final String SAMPLE_PIN_CODE = "SAMPLE_PIN_CODE";
    static final String SAMPLE_PASSWORD = "SAMPLE_PASSWORD";

    /*
    Crypto state data
     */
    private static final String KEY_PAIR_ID = "00000000-0000-4000-0000-000000000000";
    private static final String IV = "2LkZxi9J+7qudP9Nqu240Q==";
    private static final String PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqlHctzlirn4JUBMxhIpf02GiuBdEzJdxIQAsjDGPpkgPSUeA36dxJGkqZRF8145J8LrtRkFzLNldA8VY46Bt11AcPYVdGi8ADtE2SLzUSz49TPoWfuargDr4GCadpoEM5eeOLmuzVAGLTxjUJYUCqdCiK9lTq7kYDBdFcC4Lk6lfDr+UZBdlkFDxe6qE5T2pFS+XusdmAY7wFiXlTdOOzRovFngxbvHryYSLJ0/A6vFpEZipDhgUT3n/GpkNTConClDq/QNFvdkpz0oVBytd8rj66IFQUi2fTRGJLVqd+BBHIcz1pAOPkz/HEBfD/3s/ElIplj/X9xm63mpRA1FBfQIDAQAB";
    private static final String PRIVATE_KEY =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCqUdy3OWKufglQEzGEil/TYaK4F0TMl3EhACyMMY+mSA9JR4Dfp3EkaSplEXzXjknwuu1GQXMs2V0DxVjjoG3XUBw9hV0aLwAO0TZIvNRLPj1M+hZ+5quAOvgYJp2mgQzl544ua7NUAYtPGNQlhQKp0KIr2VOruRgMF0VwLguTqV8Ov5RkF2WQUPF7qoTlPakVL5e6x2YBjvAWJeVN047NGi8WeDFu8evJhIsnT8Dq8WkRmKkOGBRPef8amQ1MKicKUOr9A0W92SnPShUHK13yuProgVBSLZ9NEYktWp34EEchzPWkA4+TP8cQF8P/ez8SUimWP9f3GbrealEDUUF9AgMBAAECggEBAI3jdtF5UBl3zEBWTQlS0bPigdGFKP6cJ1XLtFDytO5c18c/d0BbWOlxZy0dnXhiBH007Jh7y/yP9GpAzOWM8BTQm0YxRTeb8htl9Sgxj1ZDuoPXUEhV2IgSGD7eZLWiiuE4v30yogmY47p4P7wmC+r2cTPAcHD92Oext1pWMbTHp0L/pGGqMn6aveaL5FG+IqyMqAe52LcyKrFn2AGobisT+1RdhV00jo2hSpmqE2FvTPHCDIVAmVM9Gfd1howQOqtG3EaED2HECtO9h9EvS0r+P3jiyO8L5LE46xXaKh4q3JLMn/WD6iTtVPy3sGh4rLcX3Ta1Gg4xlQVhgbH8DW0CgYEA31T2NAEq/dgUagp9YHTCa31ccZ0Xz6oRtClO3UENVtHHMW7yngU2YSH5t87ID/wVo3vbDOsLFKKAq0z3jXTOJ0xjn0cGkfiCrqikltJg7nADBbLQkqJ6Mzc30DRkAiBcsfm5WUzgqDpooZeNEkuOCBjq9DVcMCUIo6jCR5MyMZ8CgYEAwzvF2A9RT0EAUQaqCVzYyqfNk2TLLeyky3y+muKdREFA9vwx1obMqXR55vlJMBf7TYtHnfgHj8QzZM11shR/v5mb4Tb7YDEl04OsL6ZRZN7AWwwgc0Fnlcrnpt6nGDDS1KjyCg9eXE7JAUqDKJM8NmYJoXITyv2a2+ZcIe9oT2MCgYEA2iN1m9QsNuexAOagEe/Z2v4Gpp6HnHoEctIKSwh4S/35L2qsJfb3Z6a08HlUZnCWfinDCvolx5D2VtuFzTBzEGWHxRKt8zQmCQ2GPh7dOOhFu3IuUZvL+myL4pzZtk3/3IMMLJJvuUHb40JRM7aC05vrGn9oPUpfBuQWZ6z50QcCgYBVCn1Ubt+pGRRNNwzsBSB9rfhTIs2KFJF3/b988gc1CwOEUjhXTOJrUcwjuySRKXESxv+MJNUOX5VPbFu/FUTMLdoDkRKQRPhIGQvwuY6s1IaPYknkSnIXgonDWysH04SSk4DStv0QUlUmFdHp47CPtYSuaWWLv0osTTyGn6UdJwKBgHBIWDbip839bdKrDs3V0hJTrDm6YWb2BYxwXBZzhkFBlLyBwHKjlsV9M+qcoJ7IYdaB+ZKSv2Fye542qzaF5hFlaieO9kOGbWwHp1S/ZhmSfeoDQCtUpEJzZFpeUpNZ+JHGPFbnSimWlGPQvsT67KDPsH1AeLZxSp6bywE3IAXa";
    private static final String ENCRYPTED_SESSION_KEY =
            "hBgf3Fyo1HObucsq44SnitvzAR0AuumH7beoRueqDsY=";

    protected static final Map<String, String> CRYPTO_HELPER_STATE_STORAGE_VALUES =
            ImmutableMap.of(
                    BankdataConstants.StorageKeys.KEY_PAIR_ID_STORAGE, KEY_PAIR_ID,
                    BankdataConstants.StorageKeys.IV_STORAGE, IV,
                    BankdataConstants.StorageKeys.PUBLIC_KEY_STORAGE, PUBLIC_KEY,
                    BankdataConstants.StorageKeys.PRIVATE_KEY_STORAGE, PRIVATE_KEY,
                    BankdataConstants.StorageKeys.SESSION_KEY_STORAGE, ENCRYPTED_SESSION_KEY);

    /*
    Content of encrypted API responses
     */
    private static final Map<String, String> ENCRYPTED_RESPONSES =
            ImmutableMap.of(
                    "NEM_ID_INIT_ENCRYPTED_RESPONSE_1",
                            readFile("decrypted_nem_id_init_response.json"),
                    "COMPLETE_ENROLL_ENCRYPTED_RESPONSE",
                            readFile("decrypted_complete_enroll_response.json"),
                    "NEM_ID_INIT_ENCRYPTED_RESPONSE_2",
                            readFile("decrypted_nem_id_init_response.json"));

    /*
    Authentication results
     */
    private static final String NEM_ID_TOKEN = "SAMPLE_NEM_ID_TOKEN";
    protected static final String NEM_ID_INSTALL_ID = "SAMPLE_INSTALL_ID";

    @Override
    protected void configure() {
        bind(NemIdIFrameControllerInitializer.class)
                .toInstance(new NemIdWireMockIframeControllerInitializer(NEM_ID_TOKEN));

        bind(RandomValueGenerator.class).toInstance(new MockRandomValueGenerator());

        bind(BankdataCryptoComponentsProvider.class)
                .toInstance(
                        new BankdataCryptoComponentsProvider() {
                            @Override
                            public BankdataCryptoHelperStateGenerator provideGenerator() {

                                PersistentStorage storage = new PersistentStorage();
                                CRYPTO_HELPER_STATE_STORAGE_VALUES.forEach(storage::put);
                                BankdataCryptoHelperState state =
                                        BankdataCryptoHelperState.loadFromStorage(storage)
                                                .orElseThrow(
                                                        () ->
                                                                new IllegalStateException(
                                                                        "Could not load state from storage"));

                                BankdataCryptoHelperStateGenerator generator =
                                        mock(BankdataCryptoHelperStateGenerator.class);
                                when(generator.generate()).thenReturn(state);
                                return generator;
                            }

                            @Override
                            public BankdataCryptoHelper provideHelper() {
                                // use spy instead of mock to test if at least some requests are
                                // correctly encrypted
                                BankdataCryptoHelper cryptoHelper = spy(new BankdataCryptoHelper());

                                // session key is encrypted with random padding so we cannot test it
                                // and we have to mock it
                                doReturn("ENCRYPTED_SESSION_KEY")
                                        .when(cryptoHelper)
                                        .getEncryptedSessionKey();

                                // mock content of encrypted responses
                                for (Map.Entry<String, String> encryptedResponseMapping :
                                        ENCRYPTED_RESPONSES.entrySet()) {
                                    String encryptedMessageKey = encryptedResponseMapping.getKey();
                                    String decryptedMessage = encryptedResponseMapping.getValue();

                                    doReturn(decryptedMessage.getBytes())
                                            .when(cryptoHelper)
                                            .decrypt(encryptedMessageKey);
                                }

                                return cryptoHelper;
                            }
                        });
    }

    @SneakyThrows
    private static String readFile(String fileName) {
        Path filePath = Paths.get(RESOURCES_BASE_DIR + "/" + fileName);
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }
}
