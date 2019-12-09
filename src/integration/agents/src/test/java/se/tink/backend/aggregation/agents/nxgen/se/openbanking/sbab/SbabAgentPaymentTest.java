package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

public class SbabAgentPaymentTest {

    private final ArgumentManager<SbabAgentPaymentTest.Arg> manager =
            new ArgumentManager<>(SbabAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() throws Exception {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("SE", "se-sbab-oauth2")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)))
                        .expectLoggedIn(false);
    }

    @Test
    public void testPayments() throws Exception {
        builder.build().testGenericPayment(createRealDomesticPayment());
    }

    private List<Payment> createRealDomesticPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new SwedishIdentifier(manager.get(Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier);

        AccountIdentifier debtorAccountIdentifier =
                new SwedishIdentifier(manager.get(Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        Amount amount = Amount.inSEK(1);
        LocalDate executionDate = LocalDate.now();
        String currency = "SEK";

        return Collections.singletonList(
                new Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .build());
    }

    private List<Payment> createListMockedDomesticPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);

            doReturn(Type.SE).when(creditor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).getAccountNumber())
                    .when(creditor)
                    .getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifier.Type.SE).when(debtor).getAccountIdentifierType();
            doReturn(Iban.random(CountryCode.SE).getAccountNumber())
                    .when(debtor)
                    .getAccountNumber();
            Amount amount = Amount.inSEK(1);
            LocalDate executionDate = LocalDate.now().plus(1, ChronoUnit.DAYS);
            String currency = "SEK";

            Payment payment =
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build();

            listOfMockedPayments.add(payment);
        }

        return listOfMockedPayments;
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private enum Arg {
        SSN, // 12 digit SSN
        DEBTOR_ACCOUNT, // Domestic Swedish account number
        CREDITOR_ACCOUNT, // Domestic Swedish account number
        SAVE_AFTER,
        LOAD_BEFORE,
    }
}
