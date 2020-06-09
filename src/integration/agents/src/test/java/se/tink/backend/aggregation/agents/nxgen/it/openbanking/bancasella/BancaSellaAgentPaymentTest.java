package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancasella;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

@Ignore
public class BancaSellaAgentPaymentTest {
    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<BancaSellaAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(BancaSellaAgentPaymentTest.Arg.values());

    @Before
    public void setup() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("it", "it-bancasella-ob")
                        .setFinancialInstitutionId("bancasella")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .setClusterId("oxford-preprod")
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRealDomesticPayment());
    }

    private List<Payment> createRealDomesticPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(BancaSellaAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(BancaSellaAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        Reference reference = new Reference("Message", "ReferenceToCreditor");

        Amount amount = Amount.inEUR(1);
        LocalDate executionDate = LocalDate.now();
        String currency = "EUR";

        return Collections.singletonList(
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(reference)
                        .build());
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic IBAN account number
        CREDITOR_ACCOUNT; // Domestic IBAN account number

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
