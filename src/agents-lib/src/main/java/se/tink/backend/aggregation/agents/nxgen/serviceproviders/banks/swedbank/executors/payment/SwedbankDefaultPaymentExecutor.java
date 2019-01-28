package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment;

import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc.RegisterPayeeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc.RegisterRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;

public class SwedbankDefaultPaymentExecutor extends BaseTransferExecutor implements PaymentExecutor {
    private final Catalog catalog;

    public SwedbankDefaultPaymentExecutor(Catalog catalog, SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper) {
        super(apiClient, transferHelper);
        this.catalog = catalog;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        // this is the current implementation for transfers, only use the last profile for transfers
        apiClient.selectTransferProfile();

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse = registerPayment(transfer);

        signAndConfirmTransfer(registeredTransfersResponse);
    }

    private Optional<String> getDestinationAccountIdForPayment(Transfer transfer, PaymentBaseinfoResponse paymentBaseinfo) {
        AccountIdentifier destinationAccount = SwedbankTransferHelper.getDestinationAccount(transfer);
        Optional<String> destinationAccountId = paymentBaseinfo.getPaymentDestinationAccountId(destinationAccount);
        if (destinationAccountId.isPresent()) {
            return destinationAccountId;
        }

        AbstractAccountEntity newDestinationAccount = createSignedPayee(transfer);
        return Optional.ofNullable(newDestinationAccount.getId());
    }

    private RegisteredTransfersResponse registerPayment(Transfer transfer) {
        PaymentBaseinfoResponse paymentBaseinfo = apiClient.paymentBaseinfo();

        AccountIdentifier sourceAccount = SwedbankTransferHelper.getSourceAccount(transfer);
        Optional<String> sourceAccountId = paymentBaseinfo.getSourceAccountId(sourceAccount);
        if (!sourceAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.SOURCE_NOT_FOUND).build();
        }

        Optional<String> destinationAccountId = getDestinationAccountIdForPayment(transfer, paymentBaseinfo);
        if (!destinationAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        RegisterTransferResponse registerTransferResponse = registerPayment(transfer, sourceAccountId,
                destinationAccountId);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers(
                registerTransferResponse.getLinks().getNextOrThrow());

        registeredTransfers.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfers.getIdToConfirm();
        if (!idToConfirm.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED).build();
        }

        return registeredTransfers;
    }

    private RegisterTransferResponse registerPayment(Transfer transfer, Optional<String> sourceAccountId,
            Optional<String> destinationAccountId) {
        try {
            return apiClient.registerPayment(
                        transfer.getAmount().getValue(),
                        transfer.getDestinationMessage(),
                        SwedbankTransferHelper.getReferenceTypeFor(transfer),
                        transfer.getDueDate(),
                        destinationAccountId.get(),
                        sourceAccountId.get());
        } catch (HttpResponseException hre) {
            throw convertExceptionIfBadPaymentDate(hre);
        }
    }

    private AbstractAccountEntity createSignedPayee(final Transfer transfer) {
        AccountIdentifier accountIdentifier = transfer.getDestination();
        if (!accountIdentifier.is(AccountIdentifier.Type.SE_PG) && !accountIdentifier.is(AccountIdentifier.Type.SE_BG)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(catalog.getString("You can only make payments to Swedish destinations"))
                    .build();
        }

        RegisterPayeeRequest registerPayeeRequest = RegisterPayeeRequest.create(accountIdentifier,
                transferHelper.getDestinationName(transfer));

        RegisterRecipientResponse registerRecipientResponse = apiClient.registerPayee(registerPayeeRequest);

        return transferHelper.signAndConfirmNewRecipient(registerRecipientResponse.getLinks(),
                findNewPayeeFromPaymentResponse(registerPayeeRequest));
    }

    /**
     * Returns a function that streams through all registered payees with a filter to find the newly added payee
     * among them.
     */
    private Function<PaymentBaseinfoResponse, Optional<AbstractAccountEntity>> findNewPayeeFromPaymentResponse(
            RegisterPayeeRequest newPayee) {
        String newPayeeType = newPayee.getType().toLowerCase();
        String newPayeeAccountNumber = newPayee.getAccountNumber().replaceAll("[^0-9]", "");

        return confirmResponse -> confirmResponse.getPayment().getPayees().stream()
                .filter(payee -> payee.getType().toLowerCase().equals(newPayeeType)
                        && payee.getAccountNumber().replaceAll("[^0-9]", "").equals(newPayeeAccountNumber))
                .findFirst()
                .map(AbstractAccountEntity.class::cast);
    }
}
