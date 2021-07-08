package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DkbAgentPaymentTest {

    private final ArgumentManager<ArgumentManager.UsernamePasswordArgumentEnum>
            usernamePasswordManager =
                    new ArgumentManager<>(ArgumentManager.UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<DkbAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(DkbAgentPaymentTest.Arg.values());

    private AgentIntegrationTest.Builder builder;

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT, // Domestic IBAN account number
        CREDITOR_ACCOUNT; // Domestic IBAN account number

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @Before
    public void setup() {
        usernamePasswordManager.before();
        creditorDebtorManager.before();

        builder =
                new AgentIntegrationTest.Builder("de", "de-dkb-ob")
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.USERNAME))
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                usernamePasswordManager.get(
                                        ArgumentManager.UsernamePasswordArgumentEnum.PASSWORD))
                        .setFinancialInstitutionId("dkb")
                        .setAppId("tink")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testSepaPayments() throws Exception {
        builder.build()
                .testTinkLinkPayment(
                        createSepaPayment().withExecutionDate(LocalDate.now().plusDays(1)).build());
    }

    private Payment.Builder createSepaPayment() {
        return createRealDomesticPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(
                "SepaReferenceToCreditor "
                        + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd 'at' HH:mm")));
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(DkbAgentPaymentTest.Arg.CREDITOR_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(DkbAgentPaymentTest.Arg.DEBTOR_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
