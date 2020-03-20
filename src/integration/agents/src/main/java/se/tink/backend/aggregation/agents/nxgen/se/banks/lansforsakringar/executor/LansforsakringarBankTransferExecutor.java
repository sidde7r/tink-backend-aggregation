package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.executor.rpc.DirectTransferRequest;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarBankTransferExecutor implements BankTransferExecutor {
    private final LansforsakringarApiClient apiClient;
    private final LansforsakringarExecutorHelper executorHelper;

    public LansforsakringarBankTransferExecutor(
            LansforsakringarApiClient apiClient, LansforsakringarExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {

        if (!executorHelper.isSourceAccountValid(transfer)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(EndUserMessage.INVALID_SOURCE)
                    .build();
        }

        if (!executorHelper.isDestinationValid(transfer)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                    .build();
        }

        if (executorHelper.isInternalTransfer(transfer)) {
            try {
                DirectTransferRequest request = executorHelper.createTransferRequest(transfer);
                apiClient.executeValidateTransfer(request);
                apiClient.executeDirectTransfer(request);
            } catch (HttpResponseException e) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(EndUserMessage.TRANSFER_EXECUTE_FAILED)
                        .build();
            }
        }
        return Optional.empty();
    }
}
