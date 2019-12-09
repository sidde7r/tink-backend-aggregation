package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class HandelsbankenAgentPaymentTest {
    private enum Arg {
        USERNAME, // 12 digit SSN
        CREDITOR_ACCOUNT, // URL (se://ccccnnnnnnnnn, se-pg://nnnnnnn/ocr, se-bg://nnnnnnn/ocr)
        DEBTOR_ACCOUNT, // URL (se://ccccnnnnnnnnn)
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        manager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-handelsbanken-ob")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .addCredentialField(CredentialKeys.SCOPE, Scope.PIS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("handelsbanken")
                        .setAppId("tink")
                        .expectLoggedIn(false);

        AccountIdentifier debtorAccount =
                AccountIdentifier.create(new URI(manager.get(Arg.DEBTOR_ACCOUNT)));
        AccountIdentifier creditorAccount =
                AccountIdentifier.create(new URI(manager.get(Arg.CREDITOR_ACCOUNT)));

        builder.build()
                .testGenericPayment(
                        Collections.singletonList(
                                createTestPayment(debtorAccount, creditorAccount)));
    }

    private Payment createTestPayment(
            AccountIdentifier debtorAccount, AccountIdentifier creditorAccount) {
        Debtor debtor = new Debtor(debtorAccount);
        Creditor creditor = new Creditor(creditorAccount, creditorAccount.getName().orElse(null));

        Amount amount = Amount.inSEK(1);
        LocalDate executionDate = LocalDate.now();
        Reference reference = new Reference(null, "Testing PIS");

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(amount.getCurrency())
                .withReference(reference)
                .build();
    }
}
