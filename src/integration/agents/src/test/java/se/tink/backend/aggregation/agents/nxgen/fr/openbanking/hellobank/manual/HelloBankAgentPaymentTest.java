package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.hellobank.manual;

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
public class HelloBankAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<HelloBankAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(HelloBankAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-hellobank-ob")
                        .setFinancialInstitutionId("hellobank")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
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
                        creditorDebtorManager.get(HelloBankAgentPaymentTest.Arg.CREDITOR_ACCOUNT));

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(HelloBankAgentPaymentTest.Arg.DEBTOR_ACCOUNT));

        return new Payment.Builder()
                .withCreditor(new Creditor(creditorAccountIdentifier))
                .withDebtor(new Debtor(debtorAccountIdentifier))
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                .withCurrency("EUR")
                .withPaymentScheme(paymentScheme)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withUniqueId(UUID.randomUUID().toString())
                .build();
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

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
