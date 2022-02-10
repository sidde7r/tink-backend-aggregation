package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.OwnRecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils.IcaBankenFormatUtils;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenBankTransferExecutor implements BankTransferExecutor {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenExecutorHelper executorHelper;
    private final TransferMessageFormatter transferMessageFormatter;

    public IcaBankenBankTransferExecutor(
            IcaBankenApiClient apiClient, IcaBankenExecutorHelper executorHelper, Catalog catalog) {
        this.apiClient = apiClient;
        this.executorHelper = executorHelper;
        this.transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        IcaBankenFormatUtils.TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(
                                IcaBankenConstants.Transfers.WHITELISTED_MSG_CHARS));
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) {

        executorHelper.checkForUnsigedTransfersAndCleanUpOutbox(apiClient.fetchUnsignedTransfers());
        executorHelper.validateNoUnsignedTransfers();

        Collection<AccountEntity> transferSourceAccounts =
                apiClient.fetchAccounts().getTransferSourceAccounts();
        AccountEntity sourceAccount =
                executorHelper.findSourceAccount(transfer.getSource(), transferSourceAccounts);

        RecipientEntity destinationAccount = getRecipient(transfer, transferSourceAccounts);

        TransferRequest transferRequest =
                TransferRequest.createTransferRequest(
                        transfer, sourceAccount, destinationAccount, transferMessageFormatter);
        executeBankTransfer(transferRequest, transfer, sourceAccount);
        return Optional.empty();
    }

    /**
     * First try to find transfer destination among users' own accounts, if not found, go through
     * recipient list. If transfer destination is not found among user's saved recipients will try
     * to add recipient, if that fails throw INVALID_DESTINATION error.
     */
    private RecipientEntity getRecipient(Transfer transfer, Collection<AccountEntity> ownAccounts) {
        RecipientEntity destinationAccount;

        AccountIdentifier transferDestination = transfer.getDestination();
        Optional<AccountEntity> ownAccount =
                IcaBankenExecutorUtils.tryFindOwnAccount(transferDestination, ownAccounts);

        if (ownAccount.isPresent()) {
            destinationAccount = new OwnRecipientEntity(ownAccount.get());
        } else {
            destinationAccount = executorHelper.findDestinationAccount(transferDestination);
        }

        return destinationAccount;
    }

    /**
     * Put transfer in user's outbox. Internal transfers are executed when put in outbox, external
     * transfers requires signing with bankID in order to be executed.
     */
    private void executeBankTransfer(
            TransferRequest transferRequest, Transfer transfer, AccountEntity sourceAccount) {
        executorHelper.putTransferInOutbox(transferRequest);

        if (executorHelper.hasUnsignedTransfers()) {
            executorHelper.signTransfer(transfer, sourceAccount);
        }
    }
}
