package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.HtmlLocators.LOC_BANK_ID_METHOD_BUTTON;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.AuthenticationsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

public class NordeaBankIdIFrameInitializerTest {

    private static final String SAMPLE_STATE = "sample_state";
    private static final String SAMPLE_NONCE = "sample_nonce";
    private static final String SAMPLE_CODE_VERIFIER = "sample_code_verifier";

    private static final String TEST_DATA_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources";
    private static final File AUTHENTICATION_RESPONSE_FILE =
            Paths.get(TEST_DATA_DIR, "authenticationResponse.json").toFile();
    private static final String SAMPLE_BANK_ID_INTEGRATION_URL = "https://bank.id.integration.url";
    private static final String SAMPLE_SESSION_ID = "sample_session_id";

    private static final String SAMPLE_BANK_ID_AUTHENTICATION_URL =
            "https://bank.id.authentication.url";

    /*
    Mocks
     */
    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;
    private RandomValueGenerator randomValueGenerator;

    private BankIdWebDriver driver;
    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private NordeaBankIdIFrameInitializer iFrameInitializer;

    @Before
    public void setup() {
        authenticationClient = mock(AuthenticationClient.class);
        storage = mock(NordeaNoStorage.class);
        randomValueGenerator = mock(RandomValueGenerator.class);

        driver = mock(BankIdWebDriver.class);
        mocksToVerifyInOrder = inOrder(authenticationClient, storage, randomValueGenerator, driver);

        iFrameInitializer =
                new NordeaBankIdIFrameInitializer(
                        authenticationClient, storage, randomValueGenerator);
    }

    @Test
    public void should_initialize_bank_id_iframe() {
        // given
        when(randomValueGenerator.generateRandomBase64UrlEncoded(anyInt()))
                .thenReturn(SAMPLE_STATE)
                .thenReturn(SAMPLE_NONCE)
                .thenReturn(SAMPLE_CODE_VERIFIER);

        when(authenticationClient.getNordeaSessionDetails(any(), any(), any()))
                .thenReturn(
                        deserializeFromFile(
                                AUTHENTICATION_RESPONSE_FILE, AuthenticationsResponse.class));

        when(authenticationClient.constructUrlForBankIdAuthentication(
                        any(), any(), any(), any(), any()))
                .thenReturn(SAMPLE_BANK_ID_AUTHENTICATION_URL);

        // when
        BankIdIframeFirstWindow firstWindowForIframe = iFrameInitializer.initializeIframe(driver);

        // then
        assertThat(firstWindowForIframe).isEqualTo(BankIdIframeFirstWindow.ENTER_SSN);

        verifyCorrectBankIdAuthUrlInitialization();
        mocksToVerifyInOrder.verify(driver).getUrl(SAMPLE_BANK_ID_AUTHENTICATION_URL);
        mocksToVerifyInOrder.verify(driver).clickButton(LOC_BANK_ID_METHOD_BUTTON);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private void verifyCorrectBankIdAuthUrlInitialization() {
        mocksToVerifyInOrder
                .verify(randomValueGenerator, times(2))
                .generateRandomBase64UrlEncoded(26);
        mocksToVerifyInOrder.verify(randomValueGenerator).generateRandomBase64UrlEncoded(86);
        mocksToVerifyInOrder.verify(storage).storeCodeVerifier(SAMPLE_CODE_VERIFIER);

        String expectedCodeChallenge =
                EncodingUtils.encodeAsBase64UrlSafe(Hash.sha256(SAMPLE_CODE_VERIFIER));
        mocksToVerifyInOrder
                .verify(authenticationClient)
                .getNordeaSessionDetails(expectedCodeChallenge, SAMPLE_STATE, SAMPLE_NONCE);

        mocksToVerifyInOrder
                .verify(authenticationClient)
                .constructUrlForBankIdAuthentication(
                        expectedCodeChallenge,
                        SAMPLE_STATE,
                        SAMPLE_NONCE,
                        SAMPLE_BANK_ID_INTEGRATION_URL,
                        SAMPLE_SESSION_ID);
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private static <T> T deserializeFromFile(File file, Class<T> tClass) {
        return new ObjectMapper().readValue(file, tClass);
    }
}
