package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaBankTransferExecutor implements BankTransferExecutor {
    private final Catalog catalog;
    private NordeaSEApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaBankTransferExecutor(
            NordeaSEApiClient apiClient, Catalog catalog, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.executorHelper = executorHelper;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        final FetchAccountResponse accountResponse =
                Optional.ofNullable(apiClient.fetchAccount())
                        .orElseThrow(executorHelper::throwFailedFetchAccountsError);

        final TransferMessageFormatter transferMessageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        NordeaSEConstants.Transfer.TRANSFER_MESSAGE_LENGTH_CONFIG,
                        new StringNormalizerSwedish(
                                NordeaSEConstants.Transfer.WHITE_LISTED_CHARACTERS));

        // find source account
        final AccountEntity sourceAccount =
                executorHelper.validateSourceAccount(transfer, accountResponse, false);

        // find internal destination account
        final Optional<AccountEntity> destinationInternalAccount =
                executorHelper.validateOwnDestinationAccount(transfer, accountResponse);

        if (destinationInternalAccount.isPresent()) {
            // Internal transfers can be executed directly and doesn't need signing.
            executeInternalBankTransfer(
                    transfer,
                    sourceAccount,
                    destinationInternalAccount.get(),
                    transferMessageFormatter);
        } else {
            // create destination account
            final Optional<BeneficiariesEntity> destinationExternalAccount =
                    createDestinationAccount(transfer.getDestination());

            if (!destinationExternalAccount.isPresent()) {
                executorHelper.throwInvalidDestError();
            }
            executeExternalBankTransfer(
                    transfer,
                    sourceAccount,
                    destinationExternalAccount.get(),
                    transferMessageFormatter);
        }

        return Optional.empty();
    }

    private void executeInternalBankTransfer(
            Transfer transfer,
            AccountEntity sourceAccount,
            AccountEntity destinationInternalAccount,
            TransferMessageFormatter transferMessageFormatter) {

        InternalBankTransferRequest transferRequest = new InternalBankTransferRequest();
        transferRequest.setAmount(transfer);
        transferRequest.setFrom(sourceAccount);
        transferRequest.setTo(destinationInternalAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(transfer);

        InternalBankTransferResponse transferResponse =
                apiClient.executeInternalBankTransfer(transferRequest);

        if (!transferResponse.isTransferAccepted()) {
            executorHelper.throwTransferFailedError();
        }
    }

    private Optional<BeneficiariesEntity> createDestinationAccount(
            AccountIdentifier accountIdentifier) {
        BeneficiariesEntity destinationAccount = new BeneficiariesEntity();
        NordeaAccountIdentifierFormatter identifierFormatter =
                new NordeaAccountIdentifierFormatter();

        destinationAccount.setBankName(((SwedishIdentifier) accountIdentifier).getBankName());
        destinationAccount.setAccountNumber(accountIdentifier.getIdentifier(identifierFormatter));

        return Optional.of(destinationAccount);
    }

    private void executeExternalBankTransfer(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        // create transfer request
        PaymentRequest transferRequest =
                createPaymentRequest(
                        transfer, sourceAccount, destinationAccount, transferMessageFormatter);

        /*
         * Nordea will return an error saying unconfirmed payment already exists in outbox if
         * someone tries to create a payment to the same recipient. So we need to check if transfer
         * already exists in outbox and remove it in that case to make room for a new one
         */
        removeIfAlreadyExist(transferRequest);

        // execute external transfer
        BankPaymentResponse transferResponse = apiClient.executeBankPayment(transferRequest);

        String transferId = transferResponse.getId();
        ConfirmTransferRequest confirmTransferRequest = new ConfirmTransferRequest(transferId);
        // confirm external transfer
        executorHelper.confirm(confirmTransferRequest, transferId);
    }

    private PaymentRequest createPaymentRequest(
            Transfer transfer,
            AccountEntity sourceAccount,
            BeneficiariesEntity destinationAccount,
            TransferMessageFormatter transferMessageFormatter) {

        PaymentRequest transferRequest = new PaymentRequest();
        transferRequest.setAmount(transfer);
        transferRequest.setFrom(sourceAccount);
        transferRequest.setBankName(destinationAccount);
        transferRequest.setTo(destinationAccount);
        transferRequest.setMessage(transfer, transferMessageFormatter);
        transferRequest.setDue(transfer);
        transferRequest.setType(NordeaSEConstants.PaymentTypes.LBAN);
        transferRequest.setToAccountNumberType(NordeaSEConstants.PaymentAccountTypes.IBAN);

        return transferRequest;
    }

    private void removeIfAlreadyExist(PaymentRequest transferRequest) {
        // find first transfer in outbox that is a copy of the transferRequest object
        apiClient.fetchEInvoice().getEInvoices().stream()
                .filter(
                        entity ->
                                entity.getRecipientAccountNumber().equals(transferRequest.getTo()))
                .filter(entity -> entity.getType().equals(transferRequest.getType()))
                .filter(entity -> !entity.isConfirmed())
                .findFirst()
                .map(EInvoiceEntity::getId)
                .ifPresent(this::deleteTransfer); // delete transfer if it already exists in outbox
    }

    private void deleteTransfer(String transferId) {
        apiClient.deleteTransfer(transferId);
    }
}
