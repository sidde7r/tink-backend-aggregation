package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferHelper;
import se.tink.libraries.transfer.rpc.Transfer;

public class ValidateInternalTransferBody extends MultivaluedMapImpl {

    public ValidateInternalTransferBody(Transfer transfer, String sourceAccountNumber,
            String destinationAccountNumber) {
        add(IngConstants.Transfers.P_ACCOUNT, sourceAccountNumber);
        add(IngConstants.Transfers.B_ACCOUNT, destinationAccountNumber);
        add(IngConstants.Transfers.AMOUNT,
                IngTransferHelper.formatSignedTransferAmount(transfer.getAmount().getValue()));
        add(IngConstants.Transfers.CURRENCY, transfer.getAmount().getCurrency());
        add(IngConstants.Transfers.MEMO_DATE, IngTransferHelper.formatTransferDueDate(transfer.getDueDate()));
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(), IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
        IngTransferHelper.addDestinationMessageByMessageType(
                this, transfer.getMessageType(), transfer.getDestinationMessage());
    }
}
