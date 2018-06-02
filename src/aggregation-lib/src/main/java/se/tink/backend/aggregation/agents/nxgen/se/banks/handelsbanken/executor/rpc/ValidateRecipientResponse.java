package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.TransferAmount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.rpc.TransferSpecificationRequest.AmountableDestination;

public class ValidateRecipientResponse extends TransferableResponse implements AmountableDestination {

    private String accountNumber;
    private String accountNumberFormatted;
    private String bankName;
    
    @Override
    public TransferAmount toTransferAmount() {
        return TransferAmount.from(accountNumber, accountNumberFormatted, bankName);
    }

    @Override
    public boolean isKnownDestination() {
        return false;
    }
}
