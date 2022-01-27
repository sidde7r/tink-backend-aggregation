package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.mock;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankOBAgentWiremockTest {
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank/mock/resources";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "/configuration.yml";
    private static final String WIRE_MOCK_PAYMENT_WITH_NEW_RECIPIENT =
            RESOURCES_PATH + "/wireMock-swedbank-ob-pis.aap";
    private static final String WIRE_MOCK_CANCELLED_PAYMENT_WITH_NEW_RECIPIENT =
            RESOURCES_PATH + "/wiremock-swedbank-ob-pis-cancelled.aap";
    private static final String WIRE_MOCK_FAILED_PAYMENT_WITH_NEW_RECIPIENT =
            RESOURCES_PATH + "/wiremock-swedbank-ob-pis-failed.aap";

    private AgentsServiceConfiguration configuration;

    @Before
    public void setup() throws Exception {
        configuration = AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);
    }

    @Ignore
    @Test
    public void testPaymentWithNewRecipient() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE,
                                "se-swedbank-ob",
                                WIRE_MOCK_PAYMENT_WITH_NEW_RECIPIENT)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Ignore
    @Test(expected = PaymentAuthorizationException.class)
    public void testCancelledPaymentWithNewRecipient() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE,
                                "se-swedbank-ob",
                                WIRE_MOCK_CANCELLED_PAYMENT_WITH_NEW_RECIPIENT)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Ignore
    @Test(expected = InsufficientFundsException.class)
    public void testFailedPaymentWithNewRecipient() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.SE,
                                "se-swedbank-ob",
                                WIRE_MOCK_FAILED_PAYMENT_WITH_NEW_RECIPIENT)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .buildWithLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        Debtor debtor =
                new Debtor(AccountIdentifier.create(AccountIdentifierType.SE, "10987654321"));
        Creditor creditor =
                new Creditor(
                        AccountIdentifier.create(AccountIdentifierType.SE, "12345678901"),
                        "TinkTest");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("tinkTest");

        return new Payment.Builder()
                .withDebtor(debtor)
                .withCreditor(creditor)
                .withExecutionDate(LocalDate.of(2021, 1, 26))
                .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1))
                .withRemittanceInformation(remittanceInformation)
                .build();
    }

    @Test
    public void testFullAuthRefresh() throws Exception {

        // given
        final String wireMockServerFilePath = RESOURCES_PATH + "/swedbank_ob_fullauth_wiremock.aap";
        final String contractFilePath = RESOURCES_PATH + "/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("se-swedbank-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
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
    public void testAutoAuthRefresh() throws Exception {

        // given
        final String wireMockServerFilePath = RESOURCES_PATH + "/swedbank_ob_autoauth_wiremock.aap";
        final String contractFilePath = RESOURCES_PATH + "/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("se-swedbank-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addPersistentStorageData(
                                SwedbankConstants.StorageKeys.CONSENT, "dummyConsentId")
                        .addPersistentStorageData(
                                PersistentStorageKeys.OAUTH_2_TOKEN,
                                OAuth2Token.createBearer(
                                        "dummyAccessToken", "dummyRefreshToken", 0))
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
    public void testRefreshAccountCheck() throws Exception {

        // given
        final String wireMockServerFilePath =
                RESOURCES_PATH + "/swedbank_ob_wiremock_account_check.aap";
        final String contractFilePath = RESOURCES_PATH + "/agent-contract.json";
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.SE)
                        .withProviderName("se-swedbank-ob")
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.CHECKING_ACCOUNTS)
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }
}
