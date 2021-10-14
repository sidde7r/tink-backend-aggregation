package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import java.io.File;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities.BusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTestServer;

@RunWith(MockitoJUnitRunner.class)
public class FortisApiClientIntegrationTest {

    @Mock private FortisRandomTokenGenerator randomTokenGenerator;

    private WireMockIntegrationTestServer wireMockServer;

    private FortisApiClient fortisApiClient;

    @Before
    public void setUp() {
        when(randomTokenGenerator.generateCSRF()).thenReturn("randomly-generated-csrf-string");
    }

    @After
    public void cleanUp() {
        wireMockServer.shutdown();
    }

    private void initializeWireMockScenario(File scenarioAapFile) {
        wireMockServer = new WireMockIntegrationTestServer(scenarioAapFile);

        initializeFortisTinkHttpClient();
    }

    private void initializeFortisTinkHttpClient() {
        fortisApiClient =
                new FortisApiClient(
                        wireMockServer.getTinkHttpClient(),
                        "https://app.easybanking.bnpparibasfortis.be",
                        "49FB001",
                        randomTokenGenerator);

        FortisAgent.configureHttpClient(wireMockServer.getTinkHttpClient());
    }

    /** Delete after introduction of Agent-level WireMock tests. */
    @Test
    public void shouldFetchAccounts() {

        // given
        initializeWireMockScenario(resource("fetch-accounts.aap"));

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
        initializeWireMockScenario(resource("fetch-accounts-with-pew-code.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldFetchAccountsIgnoringUnknownPewCode() {

        // given
        initializeWireMockScenario(resource("fetch-accounts-with-unknown-pew-code.aap"));

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
        initializeWireMockScenario(resource("fetch-accounts-too-many-requests.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldThrowOnServerError() {

        // given
        initializeWireMockScenario(resource("fetch-accounts-server-error.aap"));

        // expect
        assertThatThrownBy(() -> fortisApiClient.fetchAccounts())
                .isExactlyInstanceOf(HttpResponseException.class);
    }

    private static File resource(String filename) {
        return Paths.get(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/fortis/resources")
                .resolve(filename)
                .toFile();
    }
}
