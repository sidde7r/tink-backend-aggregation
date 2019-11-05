package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.cemah;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class CemahAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("pt", "pt-cemah-oauth2")
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
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            //
            // doReturn(Iban.random(CountryCode.SE).toString()).when(creditor).getAccountNumber();
            doReturn("PT50005900012230190001669").when(creditor).getAccountNumber();
            doReturn("Tink AB").when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).toString()).when(debtor).getAccountNumber();
            //      doReturn("PT50005900012230190001669").when(debtor).getAccountNumber();

            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(
                                    new ExactCurrencyAmount(new BigDecimal(1), currency))
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
