package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.agent;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class DnbAgentPaymentTest {

    private final ArgumentManager<PsuIdArgumentEnum> manager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("no", "no-dnb-ob")
                        .addCredentialField("PSU-ID", manager.get(PsuIdArgumentEnum.PSU_ID))
                        .setFinancialInstitutionId("dnb")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifierType.NO).when(creditor).getAccountIdentifierType();
            doReturn("EnterAccountNumberHere").when(creditor).getAccountNumber();
            doReturn("Lars").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.NO).when(debtor).getAccountIdentifierType();
            doReturn("EnterAccountNumberHere").when(debtor).getAccountNumber();

            ExactCurrencyAmount exactCurrencyAmount =
                    new ExactCurrencyAmount(new BigDecimal(1), "NOK");
            LocalDate executionDate = LocalDate.now();

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(exactCurrencyAmount)
                            .withExecutionDate(executionDate)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg {
        PSU_ID,
    }
}
