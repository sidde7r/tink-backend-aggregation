package se.tink.backend.aggregation.agents.banks.sbab.manual;

import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

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
