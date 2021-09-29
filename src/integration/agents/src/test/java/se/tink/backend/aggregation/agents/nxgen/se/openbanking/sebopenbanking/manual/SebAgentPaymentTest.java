package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.manual;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.Collections;
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
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

// DISCLAIMER! Actual money being transferred, run under own responsability
public class SebAgentPaymentTest {

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
                new AgentIntegrationTest.Builder("se", "se-seb-ob")
                        .setAppId("tink")
                        .setFinancialInstitutionId("seb")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .expectLoggedIn(true)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRealDomesticPayment() throws Exception {
        builder.build().testGenericPayment(createRealDomesticPayment());
    }

    @Test
    public void testRealBgPayment() throws Exception {
        builder.build().testGenericPayment(createRealBgPayment());
    }

    @Test
    public void testCancelBankTransfer() throws Exception {
        builder.build().testCancelPayment(createCancellablePayment());
    }

    private List<Payment> createRealDomesticPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Shankey Jain");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("ToSomeone");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
        LocalDate executionDate = LocalDate.now().plusDays(2);
        String currency = "SEK";

        return Collections.singletonList(
                new Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }

    private List<Payment> createRealBgPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new BankGiroIdentifier(
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT));
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Shankey Jain");

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT));
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.OCR);
        remittanceInformation.setValue("50000038393");

        ExactCurrencyAmount amount = ExactCurrencyAmount.inSEK(1);
        LocalDate executionDate = LocalDate.now().plusDays(1);
        String currency = "SEK";

        return Collections.singletonList(
                new Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }

    private Payment createCancellablePayment() {
        Creditor creditor = mock(Creditor.class);
        doReturn(AccountIdentifierType.SE_BG).when(creditor).getAccountIdentifierType();
        return new Payment.Builder()
                .withUniqueId("STOHYD01210628132336128690000001")
                .withCreditor(creditor)
                .withType(PaymentType.DOMESTIC)
                .build();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
