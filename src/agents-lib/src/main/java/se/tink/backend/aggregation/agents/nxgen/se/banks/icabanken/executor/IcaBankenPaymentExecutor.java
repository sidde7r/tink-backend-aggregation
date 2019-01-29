package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import java.util.Collection;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenPaymentExecutor implements PaymentExecutor {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenExecutorHelper executorHelper;

    public IcaBankenPaymentExecutor(IcaBankenApiClient apiClient, IcaBankenExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        executorHelper.validateNoUnsignedTransfers();

        Collection<AccountEntity> ownAccounts = apiClient.fetchAccounts().getOwnAccounts();
        AccountEntity sourceAccount = executorHelper.findSourceAccount(transfer.getSource(), ownAccounts);

        RecipientEntity destinationAccount = executorHelper.findDestinationAccount(transfer.getDestination());

        PaymentRequest paymentRequest = PaymentRequest.create(transfer, sourceAccount, destinationAccount);

        executorHelper.putTransferInOutbox(paymentRequest);

        executorHelper.signTransfer(transfer, sourceAccount);
    }
}
