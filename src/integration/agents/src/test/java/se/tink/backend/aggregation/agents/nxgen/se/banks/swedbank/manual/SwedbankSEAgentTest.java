package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.manual;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankSEAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "swedbank-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .expectLoggedIn(false);

    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toAccountFromAccountManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        ssnManager.before();
        toAccountFromAccountManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .build()
                .testGenericPayment(createListMockedPayment(1));
    }

    private List<Payment> createListMockedPayment(int numberOfMockedPayments) {
        List<Payment> listOfMockedPayments = new ArrayList<>();

        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor = mock(Creditor.class);
            doReturn(AccountIdentifierType.SE).when(creditor).getAccountIdentifierType();
            doReturn(toAccountFromAccountManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT))
                    .when(creditor)
                    .getAccountNumber();

            Debtor debtor = mock(Debtor.class);
            doReturn(AccountIdentifierType.SE).when(debtor).getAccountIdentifierType();
            doReturn(toAccountFromAccountManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT))
                    .when(debtor)
                    .getAccountNumber();
            LocalDate executionDate = LocalDate.now();
            String currency = "SEK";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(new ExactCurrencyAmount(BigDecimal.ONE, "SEK"))
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }

    @Test
    public void testTransfers() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .doLogout(true)
                .build()
                .testBankTransfer(createMockedTransfer());
    }

    private Transfer createMockedTransfer() {
        Transfer transfer = create1SekTransfer();
        transfer.setDestination(
                new SwedishIdentifier(
                        toAccountFromAccountManager.get(
                                ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setSource(
                new SwedishIdentifier(
                        toAccountFromAccountManager.get(
                                ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        return transfer;
    }

    private Transfer create1SekTransfer() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Tink dest");
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.BANK_TRANSFER);
        // transfer.setDueDate(new Date(2020, 1, 1));
        return transfer;
    }
}
