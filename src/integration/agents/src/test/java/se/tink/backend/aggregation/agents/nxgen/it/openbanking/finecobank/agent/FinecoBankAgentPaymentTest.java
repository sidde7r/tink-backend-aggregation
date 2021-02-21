package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.agent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.LoadBeforeSaveAfterArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinecoBankAgentPaymentTest {

    private final ArgumentManager<LoadBeforeSaveAfterArgumentEnum> manager =
            new ArgumentManager<>(LoadBeforeSaveAfterArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();
        builder =
                new AgentIntegrationTest.Builder("it", "it-finecobank-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.LOAD_BEFORE)))
                        .saveCredentialsAfter(
                                Boolean.parseBoolean(
                                        manager.get(LoadBeforeSaveAfterArgumentEnum.SAVE_AFTER)));
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(1));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = Mockito.mock(Creditor.class);
            Mockito.doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random().toString()).when(creditor).getAccountNumber();
            Mockito.doReturn("Walter Bianchi").when(creditor).getName();

            RemittanceInformation remittanceInformation = new RemittanceInformation();

            Debtor debtor = Mockito.mock(Debtor.class);
            Mockito.doReturn(Type.IBAN).when(debtor).getAccountIdentifierType();
            Mockito.doReturn(Iban.random().toString()).when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(123.5);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";
            remittanceInformation.setValue("causale pagamento");

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .withRemittanceInformation(remittanceInformation)
                            .build());
        }

        return listOfMockedPayments;
    }
}
