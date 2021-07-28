package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.wiremock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.ConsentStatusFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.wiremock.module.TriodosWiremockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TriodosWiremockTest {

    static final String configurationPath =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/configuration.yml";

    @Test
    public void testRefresh() throws Exception {
        // given
        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/nl-triodos-ob-ais.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-triodos-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .addCallbackData("code", "dummyCode")
                        .withAgentTestModule(new TriodosWiremockTestModule())
                        .addCredentialField("ibans", "iban01")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoAuthRefreshTokenValidConsent() throws Exception {

        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/nl-triodos-ob-valid-consent.aap";

        final String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-triodos-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(PersistentStorageKeys.OAUTH_2_TOKEN, getToken())
                        .addPersistentStorageData("consentId", "Consent-ID")
                        .withAgentTestModule(new TriodosWiremockTestModule())
                        .addCredentialField("ibans", "iban01")
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testAutoRefreshInvalidConsent() throws Exception {

        final String wireMockServerFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/triodos/wiremock/resources/nl-triodos-ob-invalid-consent.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configurationPath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.NL)
                        .withProviderName("nl-triodos-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new TriodosWiremockTestModule())
                        .addPersistentStorageData(PersistentStorageKeys.OAUTH_2_TOKEN, getToken())
                        .addPersistentStorageData("consentId", "Consent-ID")
                        .addCredentialField("ibans", "iban01")
                        .build();

        // when
        final Throwable thrown = catchThrowable(agentWireMockRefreshTest::executeRefresh);
        // then
        assertThat(thrown).isExactlyInstanceOf(SessionException.class);
        assertThat(thrown.getStackTrace()).anyMatch(this::isThrownByConsentStatusFetcher);
    }

    private boolean isThrownByConsentStatusFetcher(StackTraceElement element) {
        return element.getMethodName().equals("validateConsent")
                && element.getClassName().equals(ConsentStatusFetcher.class.getName());
    }

    private String getToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.createBearer("accessToken", "refreshToken", 0));
    }
}
