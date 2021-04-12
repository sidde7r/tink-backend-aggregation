package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.manual;

import java.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Before;
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

public class LaBanquePostaleAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<LaBanquePostaleAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(LaBanquePostaleAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-labanquepostale-ob")
                        .setFinancialInstitutionId("labanquepostale")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testSepaPayment() throws Exception {

        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(createRealDomesticPayment(PaymentScheme.SEPA_CREDIT_TRANSFER));
    }

    @Test
    public void testSepaInstantPayment() throws Exception {

        manager.before();
        creditorDebtorManager.before();

        builder.build()
                .testTinkLinkPayment(
                        createRealDomesticPayment(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER));
    }

    private Payment createRealDomesticPayment(PaymentScheme paymentScheme) {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(
                                LaBanquePostaleAgentPaymentTest.Arg.CREDITOR_ACCOUNT));

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(
                                LaBanquePostaleAgentPaymentTest.Arg.DEBTOR_ACCOUNT));

        return new Payment.Builder()
                .withCreditor(new Creditor(creditorAccountIdentifier))
                .withDebtor(new Debtor(debtorAccountIdentifier))
                .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1.5000))
                .withCurrency("EUR")
                .withExecutionDate(LocalDate.now().plusDays(1))
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withPaymentScheme(paymentScheme)
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
