package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class FiduciaAgentPaymentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        manager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("de", "de-fiducia-oauth2")
                        .addCredentialField(CredentialKeys.IBAN, manager.get(Arg.IBAN))
                        .addCredentialField(CredentialKeys.PSU_ID, manager.get(Arg.PSU_ID))
                        .addCredentialField(CredentialKeys.PASSWORD, manager.get(Arg.PASSWORD))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor =
                    new Creditor(new IbanIdentifier(Iban.random(CountryCode.DE).toString()));

            Debtor debtor = new Debtor(new IbanIdentifier("DE39499999600000005111"));

            Amount amount = Amount.inEUR(new Random().nextInt(50000));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg {
        IBAN,
        PSU_ID,
        PASSWORD
    }
}
