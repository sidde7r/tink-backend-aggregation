package se.tink.backend.aggregation.agents.banks.seb.model;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SebTransferRequestEntityTest {
    /** Verifies that we matches the signed transfer response with a transfer request */
    @Test
    public void verifyMatchingTransfer() throws TransferMessageException {

        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(100.01));
        transfer.setSource(new SwedishIdentifier("56241111111"));
        transfer.setDestination(new SwedishIdentifier("56242222222"));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("hubba");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("hubba");

        TransferMessageFormatter mockedTransferMessageFormatter = mockTransferMessageFormatter();
        SebTransferRequestEntity transferRequest =
                SebBankTransferRequestEntity.createExternalBankTransfer(
                        transfer, "12345", mockedTransferMessageFormatter);

        BankTransferListEntity bankTransfer = new BankTransferListEntity();
        bankTransfer.SourceAccountNumber = "56241111111";
        bankTransfer.DestinationAccountNumber = "56242222222";
        bankTransfer.Amount = 100.01D;
        Assert.assertTrue(transferRequest.matches(bankTransfer));
    }

    private TransferMessageFormatter mockTransferMessageFormatter()
            throws TransferMessageException {
        TransferMessageFormatter.Messages messages =
                new TransferMessageFormatter.Messages(
                        "some formatted source message", "some formatted dest message");

        TransferMessageFormatter mock = mock(TransferMessageFormatter.class);
        when(mock.getDestinationMessageFromRemittanceInformation(any(Transfer.class), anyBoolean()))
                .thenReturn(messages.getDestinationMessage());
        when(mock.getSourceMessage(any(Transfer.class))).thenReturn(messages.getSourceMessage());
        when(mock.getMessagesFromRemittanceInformation(any(Transfer.class), anyBoolean()))
                .thenReturn(messages);

        return mock;
    }
}
