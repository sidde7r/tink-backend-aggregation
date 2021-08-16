package se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockModule.CRYPTO_HELPER_STATE_STORAGE_VALUES;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockModule.NEM_ID_INSTALL_ID;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockModule.SAMPLE_PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockModule.SAMPLE_PIN_CODE;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk.mock.BankdataDkWireMockModule.SAMPLE_USER_ID;

import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;

public class BankdataDkWireMockTest {

    static final String RESOURCES_BASE_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/bankdatadk/mock/resources";

    private static final String CONFIG_PATH = getResourcePath("configuration.yml");

    @Test
    @SneakyThrows
    public void testAuthenticateManual() {
        // given
        AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_PATH);

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DK)
                        .withProviderName("dk-sydbank-password")
                        .withWireMockFilePath(getResourcePath("bankdata_authenticate_manual.aap"))
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .withAgentTestModule(new BankdataDkWireMockModule())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), SAMPLE_USER_ID)
                        .addCredentialField(Field.Key.ACCESS_PIN.getFieldKey(), SAMPLE_PIN_CODE)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), SAMPLE_PASSWORD)
                        .build();

        // then
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test
    @SneakyThrows
    public void testRefreshWithAutoAuthentication() {
        // given
        AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIG_PATH);

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DK)
                        .withProviderName("dk-sydbank-password")
                        .withWireMockFilePath(getResourcePath("bankdata_refresh.aap"))
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .withAgentTestModule(new BankdataDkWireMockModule())
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), SAMPLE_USER_ID)
                        .addCredentialField(Field.Key.ACCESS_PIN.getFieldKey(), SAMPLE_PIN_CODE)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), SAMPLE_PASSWORD)
                        .addPersistentStorageData(CRYPTO_HELPER_STATE_STORAGE_VALUES)
                        .addPersistentStorageData(
                                NemIdConstants.Storage.NEMID_INSTALL_ID, NEM_ID_INSTALL_ID)
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(
                                getResourcePath("bankdata_refresh_contract.json"));

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    private static String getResourcePath(String fileName) {
        return RESOURCES_BASE_DIR + "/" + fileName;
    }
}
