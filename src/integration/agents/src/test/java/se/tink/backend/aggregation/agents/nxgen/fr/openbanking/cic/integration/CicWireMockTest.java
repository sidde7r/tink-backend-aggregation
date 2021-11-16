package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.cic.integration;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.cic.integration.module.CicWireMockTestModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CicWireMockTest {

    private static final String CONFIGURATION_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/configuration.yml";

    @Test
    public void shouldAutoRefresh() throws Exception {
        // given
        String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_accounts_transfers.aap";

        String contractFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/contract.json";

        AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-cic-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .withAgentTestModule(new CicWireMockTestModule())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .enableHttpDebugTrace()
                        .build();

        AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test(expected = LoginException.class)
    public void testBlockedAccess() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_blocked_access_token.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-cic-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentTestModule(new CicWireMockTestModule())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test(expected = LoginException.class)
    public void testBlockedAccessByAccountManager() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_blocked_access_token_by_account_manager.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-cic-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentTestModule(new CicWireMockTestModule())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test(expected = LoginException.class)
    public void testNoAccountsException() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_no_accounts.app";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-cic-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentTestModule(new CicWireMockTestModule())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test(expected = BankServiceException.class)
    public void testInternalServerError() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_internal_server_error.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.FR)
                        .withProviderName("fr-cic-ob")
                        .withWireMockFilePath(wireMockFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .withRefreshableItems(RefreshableItem.REFRESHABLE_ITEMS_ALL)
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .withAgentTestModule(new CicWireMockTestModule())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addPersistentStorageData("oauth2_access_token", getToken())
                        .enableHttpDebugTrace()
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.FR, "fr-cic-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createMockedDomesticPayment(
                                        PaymentScheme.SEPA_CREDIT_TRANSFER,
                                        LocalDate.of(2021, 4, 20)))
                        .withAgentModule(new CicWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testInstantPayment() throws Exception {
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/cic/integration/resources/cic_instant_payment_mock_log.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.FR, "fr-cic-ob", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .withHttpDebugTrace()
                        .withPayment(
                                createMockedDomesticPayment(
                                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER, null))
                        .withAgentModule(new CicWireMockTestModule())
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then (execution and assertion currently done in the same step)
        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment(
            PaymentScheme paymentScheme, LocalDate executionDate) {
        String currency = "EUR";
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", currency);
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Message");

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "FR1420041010050500013M02606"),
                                "Payment Receiver"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, "FR1261401750597365134612940")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(executionDate)
                .withUniqueId("paymentId")
                .withPaymentScheme(paymentScheme)
                .build();
    }

    private String getToken() {
        return SerializationUtils.serializeToString(
                OAuth2Token.create("refreshToken", "accessToken", "refreshToken", 90));
    }
}
