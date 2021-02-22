package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.agent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.IbanArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.PsuIdArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class FiduciaAgentPaymentTest {

    private final ArgumentManager<IbanArgumentEnum> ibanManager =
            new ArgumentManager<>(IbanArgumentEnum.values());
    private final ArgumentManager<PsuIdArgumentEnum> psuIdManager =
            new ArgumentManager<>(PsuIdArgumentEnum.values());
    private final ArgumentManager<PasswordArgumentEnum> passwordManager =
            new ArgumentManager<>(PasswordArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        psuIdManager.before();
        passwordManager.before();
        ibanManager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("de", "de-vrbank-raiba-muc-sued-ob")
                        .addCredentialField(
                                CredentialKeys.IBAN, ibanManager.get(IbanArgumentEnum.IBAN))
                        .addCredentialField(
                                CredentialKeys.PSU_ID, psuIdManager.get(PsuIdArgumentEnum.PSU_ID))
                        .addCredentialField(
                                CredentialKeys.PASSWORD,
                                passwordManager.get(PasswordArgumentEnum.PASSWORD))
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createListMockedDomesticPayment(4));
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor =
                    new Creditor(new IbanIdentifier(Iban.random(CountryCode.DE).toString()));

            Debtor debtor = new Debtor(new IbanIdentifier("DE39499999600000005111"));

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(new Random().nextInt(50000));
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
