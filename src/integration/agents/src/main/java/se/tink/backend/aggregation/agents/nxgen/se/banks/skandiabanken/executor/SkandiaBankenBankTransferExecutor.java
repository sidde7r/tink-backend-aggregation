package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.TransferExceptionMessage;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenBankTransferExecutor implements BankTransferExecutor {

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(TransferExceptionMessage.A2A_NOT_SUPPORTED)
                .setEndUserMessage(EndUserMessage.END_USER_WRONG_PAYMENT_TYPE)
                .setInternalStatus(InternalStatus.INVALID_PAYMENT_TYPE.toString())
                .build();
    }
}
