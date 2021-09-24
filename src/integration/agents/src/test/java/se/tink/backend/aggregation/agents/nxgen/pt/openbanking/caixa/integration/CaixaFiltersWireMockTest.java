package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.dropwizard.configuration.ConfigurationException;
import java.io.IOException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.module.SibsWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

@RunWith(JUnitParamsRunner.class)
public class CaixaFiltersWireMockTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/caixa/integration/resources/";
    private static final String CONFIGURATION_FILE_PATH = RESOURCE_PATH + "configuration.yml";

    private static final String CONSENT_INVALID_SIBS_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_INVALID\",\"text\":\"The consent definition is not complete or invalid. In case of being not complete, the bank is not supporting a completion of the consent towards the PSU. Additional information will be provided.\"}]}";

    private static final String SERVICE_INVALID_SIBS_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"SERVICE_INVALID\",\"text\":\"The addressed service is not valid for the addressed resources.\"}]}";

    private static final String ACCESS_EXCEEDED_SIBS_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"ACCESS_EXCEEDED\",\"text\":\"The access on the account has been exceeding the consented multiplicity per day.\"}]}";

    private static final String RATE_LIMIT_SIBS_MESSAGE =
            "{ \"httpCode\":\"429\", \"httpMessage\":\"Too Many Requests\", \"moreInformation\":\"Rate Limit exceeded\" }";

    private static final String SERVICE_UNAVAILABLE_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"SERVICE_UNAVAILABLE\",\"text\":\"The ASPSP server is currently unavailable. Generally, this is a temporary state.\"}]}";

    private static final String BAD_REQUEST_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"BAD_REQUEST\"}]}";

    private static final String BAD_GATEWAY_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"BAD_GATEWAY\"}]}";

    private final AgentsServiceConfiguration configuration = getConfiguration();

    private static AgentsServiceConfiguration getConfiguration() {
        try {
            return AgentsServiceConfigurationReader.read(CONFIGURATION_FILE_PATH);
        } catch (IOException | ConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenAccessIsExceeded() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_access_exceeded.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 429 + " Error body: " + ACCESS_EXCEEDED_SIBS_MESSAGE)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.ACCESS_EXCEEDED.exception());
    }

    @Test
    public void shouldThrowSessionExceptionWhenConsentIsInvalid() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_consent_invalid.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 401 + " Error body: " + CONSENT_INVALID_SIBS_MESSAGE)
                .usingRecursiveComparison()
                .isEqualTo(SessionError.CONSENT_INVALID.exception());
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenServiceIsInvalid() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_service_invalid.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 405 + " Error body: " + SERVICE_INVALID_SIBS_MESSAGE)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.BANK_SIDE_FAILURE.exception());
    }

    @Test
    public void shouldRetryAndThrowBankServiceExceptionWhenServiceIsUnavailable() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_service_unavailable.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 503 + ";Response: " + SERVICE_UNAVAILABLE_MESSAGE)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.NO_BANK_SERVICE.exception());
    }

    @Test
    public void shouldRetryAndThrowBankServiceExceptionOnInternalServerError() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_internal_server_error.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 500)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.BANK_SIDE_FAILURE.exception());
    }

    @Test
    public void shouldRetryAndThrowBankServiceExceptionOnTooManyRequestError() {
        // given
        final String filePath = RESOURCE_PATH + "caixa_pt_too_many_requests.aap";
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage("Http status: " + 429 + " Error body: " + RATE_LIMIT_SIBS_MESSAGE)
                .usingRecursiveComparison()
                .isEqualTo(BankServiceError.ACCESS_EXCEEDED.exception());
    }

    @Test
    @Parameters(method = "retryErrorsParams")
    public void shouldRetryAndThrowExceptionOn400Or502Error(
            String file, int httpStatusCode, String errorMessage) {
        // given
        final String filePath = RESOURCE_PATH + file;
        final AgentWireMockRefreshTest agentWireMockRefreshTest = buildAgentWireMockTest(filePath);

        // when
        Throwable throwable = catchThrowable(agentWireMockRefreshTest::executeRefresh);

        // then
        assertThat(throwable)
                .hasMessage(
                        "Response statusCode: " + httpStatusCode + " with body: " + errorMessage)
                .isInstanceOf(HttpResponseException.class);
    }

    private Object[] retryErrorsParams() {
        return new Object[] {
            new Object[] {"caixa_pt_400_error.aap", 400, BAD_REQUEST_MESSAGE},
            new Object[] {"caixa_pt_502_error.aap", 502, BAD_GATEWAY_MESSAGE},
        };
    }

    private AgentWireMockRefreshTest buildAgentWireMockTest(String filePath) {
        return AgentWireMockRefreshTest.nxBuilder()
                .withMarketCode(MarketCode.PT)
                .withProviderName("pt-caixa-ob")
                .withWireMockFilePath(filePath)
                .withConfigFile(configuration)
                .testAutoAuthentication()
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .withAgentTestModule(new SibsWireMockTestModule())
                .addPersistentStorageData("sibs_manual_authentication_in_progress", "false")
                .addPersistentStorageData("ACCOUNT_SEGMENT", "\"PERSONAL\"")
                .addPersistentStorageData(
                        "CONSENT_ID",
                        "{\"consentId\":\"TEST_CONSENT\",\"consentCreated\":\"2020-09-22T10:00:48.699\"}")
                .build();
    }
}
