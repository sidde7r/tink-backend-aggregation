package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentGBCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class BarclaysAgentWireMockTest {

    private final String SOURCE_IDENTIFIER = "2314701111111";
    private final String DESTINATION_IDENTIFIER = "04000469430924";

    private static final String CONFIGURATION_PATH = "data/agents/uk/barclays/configuration.yml";
    private static final String RESTORE_PARTY_DATA_CONFIG_PATH =
            "data/agents/uk/barclays/dont-fetch-party-data-config.yml";

    public static final String AIS_ACCESS_TOKEN_KEY = "open_id_ais_access_token";
    private static final String EXPIRED_OAUTH2_TOKEN =
            "{\"expires_in\" : 0, \"issuedAt\": 1598516000, \"token_type\":\"bearer\",  \"access_token\":\"EXPIRED_DUMMY_ACCESS_TOKEN\", \"refreshToken\":\"DUMMY_REFRESH_TOKEN\"}";

    @Test
    public void shouldRunFullAuthRefreshSuccessfully() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/uk/barclays/mock_log.aap";
        final String contractFilePath = "data/agents/uk/barclays/agent-contract.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
        final Set<RefreshableItem> refreshableItems =
                new HashSet<>(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        refreshableItems.remove(RefreshableItem.TRANSFER_DESTINATIONS);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName("uk-barclays-oauth2")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(refreshableItems.toArray(new RefreshableItem[0]))
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
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
    public void shouldRestorePartyDataSuccessfully() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/uk/barclays/dont-fetch-party-data.aap";
        final String contractFilePath = "data/agents/uk/barclays/restore-party-data.json";

        final String parties =
                "[{\"PartyId\": \"00000000000000000000\", \"FullLegalName\":\"LITTLE BIG DIGITAL LTD\"}]";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(RESTORE_PARTY_DATA_CONFIG_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName("uk-barclays-oauth2")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, EXPIRED_OAUTH2_TOKEN)
                        .addPersistentStorageData(
                                ScaExpirationValidator.LAST_SCA_TIME,
                                LocalDateTime.now().minusMinutes(6).toString())
                        .addPersistentStorageData(PartyDataStorage.RECENT_PARTY_DATA_LIST, parties)
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    /** Test for agents currently with SCA expired but without party data stored */
    @Test
    public void shouldNotFetchPartyData() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/uk/barclays/dont-fetch-party-data.aap";
        final String contractFilePath = "data/agents/uk/barclays/dont-fetch-party-data.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(RESTORE_PARTY_DATA_CONFIG_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName("uk-barclays-oauth2")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .addPersistentStorageData(AIS_ACCESS_TOKEN_KEY, EXPIRED_OAUTH2_TOKEN)
                        .enableHttpDebugTrace()
                        .enableDataDumpForContractFile()
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
    public void testPayment() throws Exception {

        // given
        final String wireMockFilePath = "data/agents/uk/barclays/payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.UK, "uk-barclays-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentGBCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.SORT_CODE, DESTINATION_IDENTIFIER),
                                "Ritesh Tink"))
                .withDebtor(new Debtor(AccountIdentifier.create(Type.SORT_CODE, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "UK Demo"))
                .withUniqueId("b900555d03124056b54930e1c53c9cac")
                .build();
    }
}
