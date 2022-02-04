package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.hsbcgroup.hsbc.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class HsbcAgentWireMockTest {

    private static final String PROVIDER_NAME = "uk-hsbc-kinetic-ob";
    private static final String configFilePath = getResourceFilePath("configuration.yml");
    private static final Function<LocalDateTime, String> VALID_OAUTH2_TOKEN =
            localDateTime ->
                    String.format(
                            "{\"expires_in\" : 0, "
                                    + "\"issuedAt\": %s, "
                                    + "\"tokenType\":\"bearer\", "
                                    + "\"refreshToken\":\"DUMMY_REFRESH_TOKEN\", "
                                    + "\"accessToken\":\"DUMMY_ACCESS_TOKEN\"}",
                            localDateTime.toEpochSecond(ZoneOffset.UTC));

    @Test
    public void autoAuthentication() throws Exception {
        // given
        final String wireMockServerFilePath = getResourceFilePath("auto-auth-kinetic.aap");

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addPersistentStorageData(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                VALID_OAUTH2_TOKEN.apply(LocalDateTime.now().plusHours(1)))
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        Assertions.assertThatCode(agentWireMockRefreshTest::executeRefresh)
                .doesNotThrowAnyException();
    }

    @Test
    public void manualRefreshAccount() throws Exception {
        // given
        final String wireMockServerFilePath =
                getResourceFilePath("manual-refresh-account-kinetic.aap");
        final String wireMockContractFilePath =
                getResourceFilePath("manual-refresh-account-contract-kinetic.json");

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(wireMockContractFilePath);

        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(wireMockServerFilePath)
                        .withConfigFile(AgentsServiceConfigurationReader.read(configFilePath))
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("code", "dummyCode")
                        .build();

        // when
        agentWireMockRefreshTest.executeRefresh();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testPayment() throws Exception {
        // given
        final String wireMockFilePath = getResourceFilePath("uk-hsbc-pis.aap");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configFilePath);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.UK, "uk-hsbc-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createMockedDomesticPayment())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .buildWithoutLogin(PaymentCommand.class);
        // when
        ThrowingCallable throwingCallable = agentWireMockPaymentTest::executePayment;

        // then
        assertThatCode(throwingCallable).doesNotThrowAnyException();
    }

    @Test
    public void testCancelledPayment() throws Exception {
        // given
        final String wireMockFilePath = getResourceFilePath("cancelled_domestic_payment.aap");
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(configFilePath);

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.UK, "uk-hsbc-oauth2", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withHttpDebugTrace()
                        .withPayment(createProductionCancelledMockedDomesticPayment())
                        .addCallbackData("code", "DUMMY_AUTH_CODE")
                        .addCallbackData("error", "access_denied")
                        .buildWithoutLogin(PaymentCommand.class);

        // then
        Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    private static String getResourceFilePath(String file) {
        return "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/ukob/hsbcgroup/hsbc/mock/resources/"
                + file;
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, "12345612345678"),
                                "Test Recipient"))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "UNSTRUCTURED"))
                .withUniqueId("1f9ad8b9fa094ff5872bd7fca6d3e52a")
                .build();
    }

    private Payment createProductionCancelledMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("100.00", "GBP");
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("Donation");
        String currency = "GBP";
        return new Payment.Builder()
                .withCreditor(new Creditor(new SortCodeIdentifier("12345612345678"), "BHF"))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId("1e366a7c3dd948f3a9a2eccda375961d")
                .build();
    }
}
