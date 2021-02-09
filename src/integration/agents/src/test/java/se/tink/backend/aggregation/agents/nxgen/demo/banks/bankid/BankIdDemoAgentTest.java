package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BankIdDemoAgentTest {

    private static final String USERNAME = "180012120000";

    @Test
    public void refresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-test-bankid-qr-successful")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("se", "se-test-bankid-qr-successful")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        AccountIdentifier creditorAccountIdentifier = new SwedishIdentifier("9999-99999999999");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Recipient");

        AccountIdentifier debtorAccountIdentifier = new SwedishIdentifier("9999-111111111111");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Message");

        Amount amount = Amount.inSEK(1);
        LocalDate executionDate = LocalDate.now().plusDays(1);
        String currency = "SEK";

        final Payment payment =
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(remittanceInformation)
                        .build();

        builder.build().testTinkLinkPayment(payment);
    }
}
