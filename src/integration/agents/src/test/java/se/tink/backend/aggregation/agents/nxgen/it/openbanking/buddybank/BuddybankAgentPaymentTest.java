package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BuddybankAgentPaymentTest {
    // https://authorization.api-sandbox.unicredit.eu:8403/sandbox/psd2/bg/loginPSD2_BG.html
    // https://authorization.api-sandbox.unicredit.eu/sandbox/psd2/bg/loginPSD2_BG.html
    // PSU_ID_TYPE => "ALL"
    private final ArgumentManager<PsuIdArgumentEnum> manager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("it", "it-buddybank-oauth2")
                        .addCredentialField(
                                Key.ADDITIONAL_INFORMATION,
                                manager.get(PsuIdArgumentEnum.PSU_ID_TYPE))
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
            doReturn(Iban.random(CountryCode.IT).toString()).when(creditor).getAccountNumber();
            doReturn("Creditor Name").when(creditor).getName();

            Reference reference = mock(Reference.class);
            doReturn("Message").when(reference).getValue();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.IT).toString()).when(debtor).getAccountNumber();

            RemittanceInformation remittanceInformation = new RemittanceInformation();
            ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(new Random().nextInt(1000));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";
            remittanceInformation.setValue("Buddy");

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withType(PaymentType.DOMESTIC)
                            .withExecutionDate(executionDate)
                            .withRemittanceInformation(remittanceInformation)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
