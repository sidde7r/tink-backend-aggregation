package se.tink.backend.aggregation.agents.nxgen.es.openbanking.ing;

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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class IngAgentPaymentTest {

    private static final String CURRENCY = "EUR";
    private static final String AMOUNT = "1.00";
    private static final String IBAN_OF_THE_PERSON_WHO_GIVES_THE_MONEY = "";
    private static final String IBAN_OF_THE_PERSON_WHO_GETS_THE_MONEY = "";
    private static final String NAME_OF_THE_PERSON_WHO_GETS_THE_MONEY = "";
    private static final RemittanceInformation remittanceInformation =
            prepareMockedRemittanceInformation();
    private final LocalDate executionDate = LocalDate.now();
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("es", "es-ing-ob")
                        .setFinancialInstitutionId("5af69bd27154444ebe525d09d4bad3ae")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build()
                .testGenericPayment(
                        createListMockedDomesticPayment(1, PaymentScheme.SEPA_CREDIT_TRANSFER));
    }

    private static RemittanceInformation prepareMockedRemittanceInformation() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Payment test");
        return remittanceInformation;
    }

    private List<Payment> createListMockedDomesticPayment(
            int numberOfMockedPayments, PaymentScheme paymentScheme) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifierType.IBAN).when(creditor).getAccountIdentifierType();
            doReturn(IBAN_OF_THE_PERSON_WHO_GETS_THE_MONEY).when(creditor).getAccountNumber();
            doReturn(NAME_OF_THE_PERSON_WHO_GETS_THE_MONEY).when(creditor).getName();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(IBAN_OF_THE_PERSON_WHO_GIVES_THE_MONEY).when(debtor).getAccountNumber();

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(
                                    new ExactCurrencyAmount(new BigDecimal(AMOUNT), CURRENCY))
                            .withExecutionDate(executionDate)
                            .withCurrency(CURRENCY)
                            .withRemittanceInformation(remittanceInformation)
                            .withPaymentScheme(paymentScheme)
                            .build());
        }
        return listOfMockedPayments;
    }
}
