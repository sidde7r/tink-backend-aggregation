package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
public class RBSAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";

    private AgentIntegrationTest.Builder getAgentBuilder() {
        return new AgentIntegrationTest.Builder("uk", "uk-rbs-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .doLogout(false);
    }

    @Test
    public void testRefresh() throws Exception {
        getAgentBuilder().build().testRefresh();
    }

    @Test
    public void testBankTransfer() throws Exception {
        AccountIdentifier sourceAccount = createSortCodeIdentifier("-", "name");
        AccountIdentifier destinationAccount = createSortCodeIdentifier("-", "name");

        Transfer transfer =
                createTransfer(
                        sourceAccount,
                        destinationAccount,
                        null,
                        "Test message",
                        Amount.valueOf("GBP", 100, 2));

        getAgentBuilder().build().testBankTransfer(transfer);
    }

    private AccountIdentifier createSortCodeIdentifier(
            String sortCodeAccountNumber, String accountName) {
        AccountIdentifier identifier = new SortCodeIdentifier(sortCodeAccountNumber);
        identifier.setName(accountName);
        return identifier;
    }

    private Transfer createTransfer(
            AccountIdentifier sourceAccount,
            AccountIdentifier destinationAccount,
            Date dueDate,
            String destinationMessage,
            Amount amount) {
        Transfer transfer = new Transfer();

        transfer.setSource(sourceAccount);
        transfer.setDestination(destinationAccount);
        transfer.setAmount(amount);
        transfer.setDueDate(dueDate);
        transfer.setMessageType(MessageType.FREE_TEXT);
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestinationMessage(destinationMessage);

        return transfer;
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-rbs-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    private List<Payment> createMockedDomesticPayment() {

        List<Payment> payments = new ArrayList<>();
        Amount amount = Amount.valueOf("GBP", 100, 2);
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";

        payments.add(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                DESTINATION_IDENTIFIER),
                                        "Unknown Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                SOURCE_IDENTIFIER)))
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(new Reference("TRANSFER", "test transfer by Tink"))
                        .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                        .build());

        return payments;
    }
}
