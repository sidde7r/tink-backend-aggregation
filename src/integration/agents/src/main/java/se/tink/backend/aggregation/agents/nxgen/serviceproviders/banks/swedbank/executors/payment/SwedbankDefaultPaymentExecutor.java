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
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
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
        // We'll go through all the profiles to find the one the source account belongs to.
        // That profile will also be selected so it's used going forward in the execution flow.
        String sourceAccountId = this.getSourceAccountIdAndSelectProfile(transfer);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        registeredTransfers.noUnsignedTransfersOrThrow();

        RegisteredTransfersResponse registeredTransfersResponse = registerPayment(transfer, sourceAccountId);

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

    private RegisteredTransfersResponse registerPayment(Transfer transfer, String sourceAccountId) {
        PaymentBaseinfoResponse paymentBaseinfo = apiClient.paymentBaseinfo();

        Optional<String> destinationAccountId = getDestinationAccountIdForPayment(transfer, paymentBaseinfo);
        if (!destinationAccountId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        RegisterTransferResponse registerTransferResponse = registerPayment(transfer, sourceAccountId,
                destinationAccountId.get());

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

    private RegisterTransferResponse registerPayment(Transfer transfer, String sourceAccountId,
            String destinationAccountId) {
        try {
            return apiClient.registerPayment(
                        transfer.getAmount().getValue(),
                        transfer.getDestinationMessage(),
                        SwedbankTransferHelper.getReferenceTypeFor(transfer),
                        transfer.getDueDate(),
                        destinationAccountId,
                        sourceAccountId);
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
