package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ExecuteInternalTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateInternalTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.core.transfer.Transfer;

public class IngInternalTransferExecutor {
    private final IngApiClient apiClient;
    private final LoginResponseEntity loginResponse;

    public IngInternalTransferExecutor(IngApiClient apiClient, LoginResponseEntity loginResponse) {
        this.apiClient = apiClient;
        this.loginResponse = loginResponse;
    }

    public void executeInternalTransfer(Transfer transfer, AccountEntity sourceAccount,
            AccountEntity destinationAccount) {
        ValidateInternalTransferResponse validateTransferResponse =
                apiClient.validateInternalTransfer(loginResponse, sourceAccount.getBbanNumber(),
                        destinationAccount.getBbanNumber(), transfer);

        IngTransferHelper.verifyTransferValidationXmlResponse(validateTransferResponse);

        ExecuteInternalTransferResponse response = apiClient.executeInternalTransfer(validateTransferResponse);

        IngTransferHelper.ensureTransferExecutionWasSuccess(response.getReturnCode());
    }
}
