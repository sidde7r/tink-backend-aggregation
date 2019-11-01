package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class HandelsbankenAgentPaymentTest {

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Test
    public void testPayments() throws Exception {
        manager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-handelsbanken-ob")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(CredentialKeys.SCOPE, Scope.PIS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("handelsbanken")
                        .setAppId("tink")
                        .expectLoggedIn(false);

        builder.build().testGenericPayment(createListMockedPayment(1));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Debtor debtor = mock(Debtor.class);
            doReturn(Type.BBAN).when(debtor).getAccountIdentifierType();
            doReturn(manager.get(Arg.DEBTOR_ACCOUNT)).when(debtor).getAccountNumber();

            Creditor creditor = mock(Creditor.class);
            doReturn(Type.BBAN).when(creditor).getAccountIdentifierType();
            doReturn(manager.get(Arg.CREDITOR_ACCOUNT)).when(creditor).getAccountNumber();

            Amount amount = Amount.inSEK(5);
            LocalDate executionDate = LocalDate.now();
            String currency = "SEK";

            Reference reference = mock(Reference.class);
            doReturn("Testing PIS").when(reference).getValue();

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .withReference(reference)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg {
        USERNAME,
        CREDITOR_ACCOUNT,
        DEBTOR_ACCOUNT,
    }
}
