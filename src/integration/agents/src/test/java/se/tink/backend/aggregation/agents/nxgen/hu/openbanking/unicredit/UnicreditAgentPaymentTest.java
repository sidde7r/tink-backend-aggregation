package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class UnicreditAgentPaymentTest {

    // PSU_ID_TYPE => "ALL"
    // IBAN => "HU47109180010000007941740002"
    private final ArgumentManager<PsuIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        psuIdManager.before();
        ibanManager.before();

        builder =
                new AgentIntegrationTest.Builder("hu", "hu-unicredit-oauth2")
                        .addCredentialField(
                                Key.ADDITIONAL_INFORMATION,
                                psuIdManager.get(PsuIdArgumentEnum.PSU_ID_TYPE))
                        .addCredentialField(Key.LOGIN_INPUT, ibanManager.get(IbanArgumentEnum.IBAN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedDomesticPayment(2));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(creditor).getAccountIdentifierType();
            doReturn("HU06109180010000009347760008").when(creditor).getAccountNumber();
            doReturn("Creditor Name").when(creditor).getName();

            Reference reference = mock(Reference.class);
            doReturn("Message").when(reference).getValue();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn("HU47109180010000007941740002").when(debtor).getAccountNumber();

            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(new Random().nextInt(1000));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withType(PaymentType.DOMESTIC)
                            .withExecutionDate(executionDate)
                            .withReference(reference)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
