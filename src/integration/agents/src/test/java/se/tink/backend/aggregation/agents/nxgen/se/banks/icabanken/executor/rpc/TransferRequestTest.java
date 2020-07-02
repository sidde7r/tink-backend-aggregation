package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.hamcrest.core.Is;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferRequestTest {

    private static final String SOURCE_MESSAGE_LONGER_THAN_25_CHARS =
            "A source message to be shown in my bank";

    @Test
    public void ensureSourceMessageIsTrimmedWhenLongerThan25Characters() {
        final Transfer transfer = createTransfer(SOURCE_MESSAGE_LONGER_THAN_25_CHARS);

        final AccountEntity sourceAccount = mock(AccountEntity.class);
        final RecipientEntity destinationAccount = mock(RecipientEntity.class);
        when(sourceAccount.getAccountId()).thenReturn("7434389");
        when(destinationAccount.getAccountNumber()).thenReturn("7434389");
        when(destinationAccount.getType()).thenReturn("7434389");

        final TransferRequest paymentRequest =
                TransferRequest.createPaymentRequest(transfer, sourceAccount, destinationAccount);

        assertThat(paymentRequest.getMemo().length(), Is.is(25));
    }

    private Transfer createTransfer(String sourceMessage) {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(100.01));
        transfer.setSource(new SwedishIdentifier("56241111111"));
        transfer.setDestination(new BankGiroIdentifier("900-8004"));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Destination message");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage(sourceMessage);
        transfer.setDueDate(
                Date.from(LocalDate.of(2020, 6, 22).atStartOfDay(ZoneId.of("CET")).toInstant()));
        transfer.setType(TransferType.PAYMENT);

        return transfer;
    }
}
