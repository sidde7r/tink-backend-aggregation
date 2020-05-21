package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Before;
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

public class BnpParibasAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<BnpParibasAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(BnpParibasAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-bnpparibas-ob")
                        .setFinancialInstitutionId("bnpparibas")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
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
                        creditorDebtorManager.get(BnpParibasAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Jan Nowak");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(BnpParibasAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
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
                        .withUniqueId(UUID.randomUUID().toString())
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
