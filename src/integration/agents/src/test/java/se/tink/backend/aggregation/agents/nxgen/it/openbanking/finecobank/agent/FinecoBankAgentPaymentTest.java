package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.agent;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinecoBankAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("finecobank")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testTinkLinkPayment(createRealDomesticPayment());
    }

    private Payment createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("fineco");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier("IT95N0300203280155761664887");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        LocalDate executionDate = LocalDate.now();
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                .build();
    }
}
