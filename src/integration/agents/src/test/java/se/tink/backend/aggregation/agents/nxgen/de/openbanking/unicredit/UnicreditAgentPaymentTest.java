package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

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
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UnicreditAgentPaymentTest {

    // PSU_ID_TYPE => "HVB_ONLINEBANKING"
    // USERNAME => "bgdemo"
    // PASSWORD => "bgpassword"
    // OTP => "123456"

    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<PsuIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private final ArgumentManager<Arg> otpManager = new ArgumentManager<>(Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        otpManager.before();
        usernamePasswordManager.before();
        psuIdManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-unicredit-oauth2")
                        .addCredentialField(
                                Key.ADDITIONAL_INFORMATION,
                                psuIdManager.get(PsuIdArgumentEnum.PSU_ID_TYPE))
                        .addCredentialField(
                                Key.USERNAME,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Key.PASSWORD,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .addCredentialField(Key.OTP_INPUT, otpManager.get(Arg.OTP))
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
            doReturn(Iban.random(CountryCode.DE).toString()).when(creditor).getAccountNumber();
            doReturn("Creditor Name").when(creditor).getName();

            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setValue("Message");

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.IBAN).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.DE).toString()).when(debtor).getAccountNumber();

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
                            .withRemittanceInformation(remittanceInformation)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }

    private enum Arg implements ArgumentManagerEnum {
        OTP;

        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
