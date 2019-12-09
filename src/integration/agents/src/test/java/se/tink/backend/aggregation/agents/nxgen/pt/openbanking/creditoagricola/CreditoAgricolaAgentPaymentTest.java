package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.creditoagricola;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class CreditoAgricolaAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final String currency = "EUR";
    private final LocalDate executionDate = LocalDate.now().plusDays(1);
    private final int AMOUNT = 1;
    private final String IBAN_CREDITO_AGRICOLA = "";
    private final String IBAN_MILLENNIUM = "";
    private final String ACCOUNT_HOLDER_MILLENNIUM = "";

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-creditoagricola-oauth2")
                        .setFinancialInstitutionId("creditoagricola")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {

            // gets money:
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(IBAN_MILLENNIUM).when(creditor).getAccountNumber();
            doReturn(ACCOUNT_HOLDER_MILLENNIUM).when(creditor).getName();

            // gives money:
            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(IBAN_CREDITO_AGRICOLA).when(debtor).getAccountNumber();

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(
                                    new ExactCurrencyAmount(new BigDecimal(AMOUNT), currency))
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
