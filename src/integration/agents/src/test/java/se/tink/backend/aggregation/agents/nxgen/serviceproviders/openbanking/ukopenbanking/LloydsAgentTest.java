package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
public class LloydsAgentTest {

    private final String TRANSFER_SOURCE = "";
    private final String TRANSFER_DEST = "";

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-lloyds-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();

        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_SOURCE));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, TRANSFER_DEST));
        transfer.setAmount(Amount.valueOf("GBP", 105, 2));
        transfer.setSourceMessage("Message!");

        new AgentIntegrationTest.Builder("uk", "uk-lloyds-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testBankTransfer(transfer);
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "uk-lloyds-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPayment(createMockedDomesticPayment());
    }

    private List<Payment> createMockedDomesticPayment() {

        List<Payment> payments = new ArrayList<>();

        Creditor creditor = mock(Creditor.class);
        doReturn(Type.SORT_CODE).when(creditor).getAccountIdentifierType();
        doReturn(TRANSFER_DEST).when(creditor).getAccountNumber();

        Debtor debtor = mock(Debtor.class);
        doReturn(Type.SORT_CODE).when(debtor).getAccountIdentifierType();
        doReturn(TRANSFER_SOURCE).when(debtor).getAccountNumber();

        Amount amount = Amount.valueOf("GBP", 105, 2);
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";

        payments.add(
                new Payment.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .build());

        return payments;
    }
}
