package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinTecSystemsAgentPaymentTest {

    private final ArgumentManager<FinTecSystemsAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(FinTecSystemsAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-test-fintecsystems")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setClusterId("oxford-preprod")
                        .setFinancialInstitutionId("508f1f5e1fb311ec96210242ac130002")
                        .setAppId("tink");
    }

    private enum Arg implements ArgumentManagerEnum {
        CREDITOR_ACCOUNT; // IBAN account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now()).build());
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("ReferenceToCreditor");
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(
                                FinTecSystemsAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
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
