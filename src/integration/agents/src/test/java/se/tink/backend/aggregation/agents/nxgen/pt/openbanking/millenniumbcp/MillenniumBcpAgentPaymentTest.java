package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.millenniumbcp;

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
public class MillenniumBcpAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final String currency = "EUR";
    private final LocalDate executionDate = LocalDate.now().plusDays(1);
    private final int AMOUNT = 1;
    private final String IBAN_OF_THE_PERSON_WHO_GIVES_THE_MONEY = "";
    private final String IBAN_OF_THE_PERSON_WHO_GETS_THE_MONEY = "";
    private final String NAME_OF_THE_PERSON_WHO_GETS_THE_MONEY = "";

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-millenniumbcp-oauth2")
                        .setFinancialInstitutionId("millenniumbcp")
                        .setAppId("tink")
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(IBAN_OF_THE_PERSON_WHO_GETS_THE_MONEY).when(creditor).getAccountNumber();
            doReturn(NAME_OF_THE_PERSON_WHO_GETS_THE_MONEY).when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(IBAN_OF_THE_PERSON_WHO_GIVES_THE_MONEY).when(debtor).getAccountNumber();

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
