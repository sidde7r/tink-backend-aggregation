package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem;

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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class CredemAgentPaymentTest {
    private AgentIntegrationTest.Builder builder;

    private final String IBAN_WHO_GETS_MONEY = "";
    private final String NAME_WHO_GETS_MONEY = "";

    private final String IBAN_WHO_GIVES_MONEY = "";

    private final String currency = "EUR";
    private final int AMOUNT = 1;

    @Before
    public void setup() throws Exception {
        builder =
                new AgentIntegrationTest.Builder("it", "it-credem-oauth2")
                        .setFinancialInstitutionId("credem")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(NAME_WHO_GETS_MONEY).when(creditor).getName();
            doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(IBAN_WHO_GETS_MONEY).when(creditor).getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(IBAN_WHO_GIVES_MONEY).when(debtor).getAccountNumber();

            LocalDate executionDate = LocalDate.now();

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
