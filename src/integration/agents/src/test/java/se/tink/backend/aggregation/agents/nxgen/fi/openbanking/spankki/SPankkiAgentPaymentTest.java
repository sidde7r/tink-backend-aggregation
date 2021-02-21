package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class SPankkiAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("fi", "fi-spankki-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("s-pankki")
                        .setAppId("tink");
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int mockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < mockedPayments; i++) {
            Creditor creditor = mock(Creditor.class);
            doReturn(manager.get(Arg.CREDITOR_ACCOUNT)).when(creditor).getAccountNumber();
            doReturn(manager.get(Arg.CREDITOR_NAME)).when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(manager.get(Arg.DEBTOR_ACCOUNT)).when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExecutionDate(LocalDate.now())
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private enum Arg implements ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT,
        CREDITOR_NAME;

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
