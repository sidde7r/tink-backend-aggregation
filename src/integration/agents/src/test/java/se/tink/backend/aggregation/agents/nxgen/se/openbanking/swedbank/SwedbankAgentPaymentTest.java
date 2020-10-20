package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SwedbankAgentPaymentTest {

    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        ssnManager.before();
        toFromManager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "se-swedbank-ob")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("swedbank")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createListMockedPayment(1));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(Type.SE).when(creditor).getAccountIdentifierType();
            doReturn(toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT))
                    .when(creditor)
                    .getAccountNumber();
            Debtor debtor = mock(Debtor.class);
            doReturn(Type.SE).when(debtor).getAccountIdentifierType();
            doReturn(toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT))
                    .when(debtor)
                    .getAccountNumber();

            Amount amount = Amount.inSEK(3);

            String currency = "SEK";

            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
            remittanceInformation.setValue("tinTest");

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withCurrency(currency)
                            .withRemittanceInformation(remittanceInformation)
                            .build());
        }

        return listOfMockedPayments;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
