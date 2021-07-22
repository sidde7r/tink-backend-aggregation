package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.agent;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@Ignore
public class OpBankAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<OpBankAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(OpBankAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-opbank-ob")
                        .setFinancialInstitutionId("c758d66003e5493489b9c314d09e86bc")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);
    }

    @Test
    public void testSepaPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(createRealDomesticPayment(PaymentScheme.SEPA_CREDIT_TRANSFER));
    }

    @Test
    public void testSepaInstantPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticPayment(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER));
    }

    private Payment createRealDomesticPayment(final PaymentScheme paymentScheme) {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(OpBankAgentPaymentTest.Arg.CREDITOR_ACCOUNT));

        String creditorName = creditorDebtorManager.get(Arg.CREDITOR_NAME);

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(OpBankAgentPaymentTest.Arg.DEBTOR_ACCOUNT));

        return new Payment.Builder()
                .withCreditor(new Creditor(creditorAccountIdentifier, creditorName))
                .withDebtor(new Debtor(debtorAccountIdentifier))
                .withExactCurrencyAmount(new ExactCurrencyAmount(BigDecimal.valueOf(1.55), "EUR"))
                .withCurrency("EUR")
                .withPaymentScheme(paymentScheme)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "PIS Agent OP"))
                .withUniqueId(UUID.randomUUID().toString())
                .build();
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT,
        CREDITOR_NAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
