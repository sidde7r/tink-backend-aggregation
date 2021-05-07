package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import java.time.LocalDate;
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
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
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
    public void testBankTransfer() throws Exception {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("tinkTest");

        builder.build()
                .testGenericPayment(
                        createListMockedPayment(
                                1, AccountIdentifierType.SE, remittanceInformation));
    }

    @Test
    public void testPayments() throws Exception {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("13077598319");

        builder.build()
                .testGenericPayment(
                        createListMockedPayment(
                                1, AccountIdentifierType.SE_BG, remittanceInformation));
    }

    private List<Payment> createListMockedPayment(
            int numberOfMockedPayments,
            AccountIdentifierType creditorType,
            RemittanceInformation remittanceInformation) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {

            AccountIdentifier sourceAccountIdentifier =
                    AccountIdentifier.create(
                            AccountIdentifierType.IBAN,
                            toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT));

            AccountIdentifier destinationAccountIdentifier =
                    AccountIdentifier.create(
                            creditorType,
                            toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT));

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(new Creditor(destinationAccountIdentifier, "TinkTest"))
                            .withDebtor(new Debtor(sourceAccountIdentifier))
                            .withExactCurrencyAmount(ExactCurrencyAmount.inSEK(1))
                            .withCurrency("SEK")
                            .withExecutionDate(LocalDate.now().plusDays(4))
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
