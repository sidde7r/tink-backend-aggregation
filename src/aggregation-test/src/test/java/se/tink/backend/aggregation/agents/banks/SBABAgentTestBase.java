package se.tink.backend.aggregation.agents.banks;

import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.date.DateUtils;

public class SBABAgentTestBase {
    static Transfer create1SEKTransfer() {
        Transfer transfer = new Transfer();

        transfer.setAmount(Amount.inSEK(1.0));
        transfer.setDestinationMessage("Tink dest");
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.getToday());

        return transfer;
    }
}
