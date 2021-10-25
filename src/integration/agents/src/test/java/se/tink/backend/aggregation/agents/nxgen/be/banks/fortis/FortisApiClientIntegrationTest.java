package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import java.io.File;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.BusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTestServer;

@RunWith(MockitoJUnitRunner.class)
public class FortisApiClientIntegrationTest {

    private static final WireMockIntegrationTestServer WIREMOCK_SERVER =
            new WireMockIntegrationTestServer();

    @Mock private FortisRandomTokenGenerator randomTokenGenerator;

    private FortisApiClient fortisApiClient;

    @Before
    public void setUp() {
        initMocks();
        initFortisApiHttpClient();
    }

    @AfterClass
    public static void cleanUpClass() {
        WIREMOCK_SERVER.shutdown();
    }

    /** Delete after introduction of Agent-level WireMock tests */
    @Test
    public void shouldFetchAccounts() {

        // given
        givenWireMockScenario(resource("fetch-accounts.aap"));

        // and
        AccountsResponse expectedAccountsResponse =
                deserializeFromString(
                        resource("fetch-accounts-contract.json"), AccountsResponse.class);

        // when
        AccountsResponse actualAccountsResponse = fortisApiClient.fetchAccounts();

        // then
        assertThat(actualAccountsResponse)
                .usingRecursiveComparison()
                .isEqualTo(expectedAccountsResponse);
    }

    @Test
    public void shouldThrowOnFetchAccountsWithCertainPewCode() {

        // given
        givenWireMockScenario(resource("fetch-accounts-with-pew-code.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldFetchAccountsIgnoringUnknownPewCode() {

        // given
        givenWireMockScenario(resource("fetch-accounts-with-unknown-pew-code.aap"));

        // and
        AccountsResponse expectedAccountsResponse =
                deserializeFromString(
                        resource("fetch-accounts-contract.json"), AccountsResponse.class);

        // when
        AccountsResponse actualAccountsResponse = fortisApiClient.fetchAccounts();

        // then
        assertThat(actualAccountsResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(BusinessMessageBulk.class)
                .isEqualTo(expectedAccountsResponse);

        // and
        assertThat(actualAccountsResponse.getBusinessMessageBulk().getPewCode())
                .isEqualTo("NOT_RECOGNIZED_PEW_CODE");
    }

    @Test
    public void shouldThrowOnTooManyRequests() {

        // given
        givenWireMockScenario(resource("fetch-accounts-too-many-requests.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldThrowOnServerError() {

        // given
        givenWireMockScenario(resource("fetch-accounts-server-error.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(HttpResponseException.class);
    }

    private void initMocks() {
        when(randomTokenGenerator.generateCSRF()).thenReturn("randomly-generated-csrf-string");
    }

    private void initFortisApiHttpClient() {
        TinkHttpClient tinkHttpClient = WIREMOCK_SERVER.createTinkHttpClient();

        fortisApiClient =
                new FortisApiClient(
                        tinkHttpClient,
                        "https://app.easybanking.bnpparibasfortis.be",
                        "49FB001",
                        randomTokenGenerator);

        FortisAgent.configureHttpClient(tinkHttpClient);
    }

    private void givenWireMockScenario(File aapFile) {
        WIREMOCK_SERVER.loadScenario(aapFile);
    }

    private static File resource(String filename) {
        return Paths.get(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/fortis/resources")
                .resolve(filename)
                .toFile();
    }
}
