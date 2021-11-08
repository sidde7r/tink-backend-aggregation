package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Frequency;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankAgentWireMockTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/demo/openbanking/demobank/mock/resources";
    private static final String SINGLE_PAYMENT_AAP = RESOURCE_PATH + "/demobank-single-payment.aap";
    private static final String SINGLE_PAYMENT_ERROR_AAP =
            RESOURCE_PATH + "/demobank-single-payment-error.aap";
    private static final String RECURRING_PAYMENT_AAP =
            RESOURCE_PATH + "/demobank-recurring-payment.aap";
    private static final String RECURRING_PAYMENT_ERROR_AAP =
            RESOURCE_PATH + "/demobank-recurring-payment-error.aap";
    private static final String SINGLE_EMBEDDED_PAYMENT_AAP =
            RESOURCE_PATH + "/demobank-single-embedded-payment.aap";
    private static final String DK_NEMID_AUTH_SUCCESSFUL_AAP =
            RESOURCE_PATH + "/demobank-dk-nemid-auth-successful.aap";
    private static final String DK_NEMID_AUTH_INVALID_CREDENTIALS_AAP =
            RESOURCE_PATH + "/demobank-dk-nemid-auth-invalid_credentials.aap";

    private static final String SOURCE_IDENTIFIER = "IT76K2958239128VVJCLBIHVDAT";
    private static final String DESTINATION_IDENTIFIER = "IT12L8551867857UFGAYZF25O4M";

    private static final String DK_NEMID_USERNAME = "12345678";
    private static final String DK_NEMID_PINCODE = "987654";

    @Test
    public void testDkNemidAuthSuccessful() {
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DK)
                        .withProviderName("dk-demobank-nemid")
                        .withWireMockFilePath(DK_NEMID_AUTH_SUCCESSFUL_AAP)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), DK_NEMID_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), DK_NEMID_PINCODE)
                        .addCallbackData("dummy_continue", "dummy_continue")
                        .build();

        assertThatCode(test::executeRefresh).doesNotThrowAnyException();
    }

    @Test
    public void testDkNemidAuthInvalidCredentials() {
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.DK)
                        .withProviderName("dk-demobank-nemid")
                        .withWireMockFilePath(DK_NEMID_AUTH_INVALID_CREDENTIALS_AAP)
                        .withoutConfigFile()
                        .testFullAuthentication()
                        .testOnlyAuthentication()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), DK_NEMID_USERNAME)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), DK_NEMID_PINCODE)
                        .addCallbackData("code", "dummy_continue")
                        .build();

        final Throwable thrown = catchThrowable(test::executeRefresh);

        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void testSinglePayment() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT,
                                "it-demobank-open-banking-redirect",
                                SINGLE_PAYMENT_AAP)
                        .withHttpDebugTrace()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test(expected = InsufficientFundsException.class)
    public void testSinglePaymentError() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT,
                                "it-demobank-open-banking-redirect",
                                SINGLE_PAYMENT_ERROR_AAP)
                        .withHttpDebugTrace()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testRecurringPayment() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT,
                                "it-demobank-open-banking-redirect",
                                RECURRING_PAYMENT_AAP)
                        .withHttpDebugTrace()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withPayment(createMockedRecurringDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test(expected = InsufficientFundsException.class)
    public void testRecurringPaymentError() throws Exception {
        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT,
                                "it-demobank-open-banking-redirect",
                                RECURRING_PAYMENT_ERROR_AAP)
                        .withHttpDebugTrace()
                        .addCallbackData("code", "DUMMY_CODE")
                        .withPayment(createMockedRecurringDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testSinglePaymentWithEmbeddedFlow() throws Exception {
        final String USERNAME = "u0001";
        final String PASSWORD = "abc123";

        AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.IT,
                                "it-demobank-open-banking-embedded",
                                SINGLE_EMBEDDED_PAYMENT_AAP)
                        .withHttpDebugTrace()
                        .addCredentialField(Field.Key.USERNAME.getFieldKey(), USERNAME)
                        .addCredentialField(Field.Key.PASSWORD.getFieldKey(), PASSWORD)
                        .addCallbackData("otpinput", "0000")
                        .withPayment(createMockedDomesticPayment())
                        .buildWithoutLogin(PaymentCommand.class);

        agentWireMockPaymentTest.executePayment();
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("0.1", "EUR");
        LocalDate executionDate = LocalDate.of(2021, 3, 22);
        String currency = "EUR";

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.SINGLE)
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, DESTINATION_IDENTIFIER),
                                "Unknown person"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }

    private Payment createMockedRecurringDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("0.3", "EUR");
        LocalDate startDate = LocalDate.of(2021, 3, 23);
        LocalDate endDate = startDate.plusWeeks(1);
        String currency = "EUR";

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Periodic payment");
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);

        return new Payment.Builder()
                .withPaymentServiceType(PaymentServiceType.PERIODIC)
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, DESTINATION_IDENTIFIER),
                                "Unknown person"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.IBAN, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(startDate)
                .withFrequency(Frequency.MONTHLY)
                .withDayOfMonth(23)
                .withStartDate(startDate)
                .withEndDate(endDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }
}
