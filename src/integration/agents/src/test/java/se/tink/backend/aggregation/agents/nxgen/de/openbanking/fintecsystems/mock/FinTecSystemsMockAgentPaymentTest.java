package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.LastError.CLIENT_ABORTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.LastError.PINNED_IBAN_NOT_FOUND;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.error.FTSException;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinTecSystemsMockAgentPaymentTest {

    private static final String CONFIGURATION_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/configuration.yml";

    @Test
    public void testSepaPaymentInitiation() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/pis.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when // then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    @Test
    public void testPaymentAuthorizationTimeoutError() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/PaymentAuthorizationTimeoutError.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when //then
        assertThatExceptionOfType(PaymentAuthorizationTimeOutException.class)
                .isThrownBy(agentWireMockPaymentTest::executePayment)
                .withMessage(CLIENT_ABORTED.getCode());
    }

    @Test
    public void testPaymentPaymentAuthenticationException() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/PaymentAuthenticationException.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when //then
        assertThatExceptionOfType(PaymentAuthenticationException.class)
                .isThrownBy(agentWireMockPaymentTest::executePayment)
                .withMessage(PaymentAuthenticationException.DEFAULT_MESSAGE);
    }

    @Test
    public void testPaymentAuthorizationFailedByUserException() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/PaymentAuthorizationFailedByUserException.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when //then
        assertThatExceptionOfType(PaymentAuthorizationFailedByUserException.class)
                .isThrownBy(agentWireMockPaymentTest::executePayment)
                .withMessage(PaymentAuthorizationFailedByUserException.MESSAGE);
    }

    @Test
    public void testPaymentValidationException() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/PaymentValidationException.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when //then
        assertThatExceptionOfType(PaymentValidationException.class)
                .isThrownBy(agentWireMockPaymentTest::executePayment)
                .withMessage(PINNED_IBAN_NOT_FOUND.getCode());
    }

    @Test
    public void testPaymentErrorWithCode422() throws Exception {
        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        // given
        final String wireMockFilePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/mock/resources/PaymentError422.aap";

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(
                                MarketCode.DE, "de-test-fintecsystems", wireMockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .buildWithoutLogin(PaymentCommand.class);
        // when //then
        assertThatExceptionOfType(FTSException.class)
                .isThrownBy(agentWireMockPaymentTest::executePayment);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE04888888880087654321");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
