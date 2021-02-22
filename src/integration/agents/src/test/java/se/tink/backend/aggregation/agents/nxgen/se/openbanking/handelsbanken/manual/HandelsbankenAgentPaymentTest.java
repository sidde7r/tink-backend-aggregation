package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.manual;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenSEConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class HandelsbankenAgentPaymentTest {
    private enum Arg implements ArgumentManagerEnum {
        CREDITOR_ACCOUNT, // URL (se://ccccnnnnnnnnn, se-pg://nnnnnnn/ocr, se-bg://nnnnnnn/ocr)
        DEBTOR_ACCOUNT; // URL (se://ccccnnnnnnnnn)

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final ArgumentManager<Arg> creditorDebtorManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> usernameManager =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testPayments() throws Exception {
        creditorDebtorManager.before();
        usernameManager.before();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-handelsbanken-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernameManager.get(UsernameArgumentEnum.USERNAME))
                        .addCredentialField(CredentialKeys.SCOPE, Scope.PIS)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("handelsbanken")
                        .setAppId("tink")
                        .expectLoggedIn(false);

        AccountIdentifier debtorAccount =
                AccountIdentifier.create(new URI(creditorDebtorManager.get(Arg.DEBTOR_ACCOUNT)));
        AccountIdentifier creditorAccount =
                AccountIdentifier.create(new URI(creditorDebtorManager.get(Arg.CREDITOR_ACCOUNT)));

        builder.build()
                .testGenericPayment(
                        Collections.singletonList(
                                createTestPayment(debtorAccount, creditorAccount)));
    }

    private Payment createTestPayment(
            AccountIdentifier debtorAccount, AccountIdentifier creditorAccount) {
        Debtor debtor = new Debtor(debtorAccount);
        Creditor creditor = new Creditor(creditorAccount, creditorAccount.getName().orElse(null));

        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
        LocalDate executionDate = LocalDate.now();
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Testing PIS");

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(amount.getCurrencyCode())
                .withRemittanceInformation(remittanceInformation)
                .build();
    }
}
