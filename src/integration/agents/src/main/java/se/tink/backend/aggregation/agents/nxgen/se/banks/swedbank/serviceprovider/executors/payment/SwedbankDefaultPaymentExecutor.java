package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.MenuItemKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ReturnValue;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.BaseTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterPayeeRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc.RegisterRecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisterTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc.RegisteredTransfersResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfileHandler;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultPaymentExecutor extends BaseTransferExecutor
        implements PaymentExecutor {
    private static final String INVALID_CHARACTERS = "[^0-9]";
    private final Catalog catalog;
    private final SwedbankStorage swedbankStorage;

    public SwedbankDefaultPaymentExecutor(
            Catalog catalog,
            SwedbankDefaultApiClient apiClient,
            SwedbankTransferHelper transferHelper,
            SwedbankStorage swedbankStorage,
            SwedbankDateUtils dateUtils) {
        super(apiClient, transferHelper, dateUtils);
        this.catalog = catalog;
        this.swedbankStorage = swedbankStorage;
    }

    @Override
    public void executePayment(Transfer transfer) throws TransferExecutionException {
        // We'll go through all the profiles to find the one the source account belongs to.
        // That profile will also be selected so it's used going forward in the execution flow.
        String sourceAccountId = this.getSourceAccountIdAndSelectProfile(transfer);

        RegisteredTransfersResponse registeredTransfers = apiClient.registeredTransfers();
        deleteUnsignedRegisteredTransfers(registeredTransfers.getRegisteredTransactions());

        RegisteredTransfersResponse registeredTransfersResponse =
                registerPayment(transfer, sourceAccountId);

        signAndConfirmTransfer(registeredTransfersResponse);
    }

    private Optional<String> getDestinationAccountIdForPayment(
            Transfer transfer, PaymentBaseinfoResponse paymentBaseinfo) {
        AccountIdentifier destinationAccount =
                SwedbankTransferHelper.getDestinationAccount(transfer);

        Optional<String> destinationAccountId =
                paymentBaseinfo.getPaymentDestinationAccountId(destinationAccount);

        if (destinationAccountId.isPresent()) {
            return destinationAccountId;
        }

        AbstractAccountEntity newDestinationAccount = createSignedPayee(transfer);
        return Optional.ofNullable(newDestinationAccount.getId());
    }

    private RegisteredTransfersResponse registerPayment(Transfer transfer, String sourceAccountId) {
        PaymentBaseinfoResponse paymentBaseinfo =
                swedbankStorage.getBankProfileHandler().getActivePaymentBaseInfo();

        Optional<String> destinationAccountId =
                getDestinationAccountIdForPayment(transfer, paymentBaseinfo);

        if (!destinationAccountId.isPresent()) {
            throw createInvalidDestinationException();
        }

        RegisterTransferResponse registerTransferResponse =
                registerPayment(transfer, sourceAccountId, destinationAccountId.get());

        RegisteredTransfersResponse registeredTransfers =
                apiClient.registeredTransfers(registerTransferResponse.getLinks().getNextOrThrow());

        registeredTransfers.oneUnsignedTransferOrThrow();

        Optional<String> idToConfirm = registeredTransfers.getIdToConfirm();

        if (!idToConfirm.isPresent()) {
            throw createTransferFailedException();
        }

        return registeredTransfers;
    }

    private TransferExecutionException createTransferFailedException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED)
                .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_REGISTER_FAILED)
                .build();
    }

    private TransferExecutionException createInvalidDestinationException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION)
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    private RegisterTransferResponse registerPayment(
            Transfer transfer, String sourceAccountId, String destinationAccountId) {

        Date dueDate = dateUtils.getTransferDateForPayments(transfer.getDueDate());

        if (transfer.getRemittanceInformation().getType() == null) {
            SwedbankTransferHelper.validateAndSetRemittanceInformationTypeFor(transfer);
        }

        try {
            return tryRegisterPayment(transfer, sourceAccountId, destinationAccountId, dueDate);
        } catch (HttpResponseException hre) {
            HttpResponse httpResponse = hre.getResponse();
            if (httpResponse.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
                if (errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.REFERENCE)
                        && errorResponse.hasErrorMessage(
                                SwedbankBaseConstants.ErrorMessage.OCR_NOT_ALLOWED)) {
                    try {
                        transfer.getRemittanceInformation()
                                .setType(RemittanceInformationType.UNSTRUCTURED);

                        return tryRegisterPayment(
                                transfer, sourceAccountId, destinationAccountId, dueDate);

                    } catch (HttpResponseException httpResponseException) {
                        throw convertExceptionIfBadPayment(httpResponseException);
                    }
                }
            }
            throw convertExceptionIfBadPayment(hre);
        }
    }

    private RegisterTransferResponse tryRegisterPayment(
            Transfer transfer, String sourceAccountId, String destinationAccountId, Date dueDate) {
        return apiClient.registerPayment(
                transfer.getAmount().getValue(),
                transfer.getRemittanceInformation(),
                dueDate,
                destinationAccountId,
                sourceAccountId);
    }

    private boolean isNotPgOrBg(AccountIdentifier accountIdentifier) {
        return !accountIdentifier.is(AccountIdentifierType.SE_PG)
                && !accountIdentifier.is(AccountIdentifierType.SE_BG);
    }

    private AbstractAccountEntity createSignedPayee(final Transfer transfer) {
        AccountIdentifier accountIdentifier = transfer.getDestination();

        if (isNotPgOrBg(accountIdentifier)) {
            throw createInvalidDestinationAccountException();
        }

        BankProfileHandler handler = swedbankStorage.getBankProfileHandler();
        handler.throwIfNotAuthorizedForRegisterAction(MenuItemKey.REGISTER_PAYEE, catalog);

        RegisterPayeeRequest registerPayeeRequest =
                RegisterPayeeRequest.create(
                        accountIdentifier, transferHelper.getDestinationName(transfer));

        RegisterRecipientResponse registerRecipientResponse =
                apiClient.registerPayee(registerPayeeRequest);

        return transferHelper.signAndConfirmNewRecipient(
                registerRecipientResponse.getLinks(),
                findNewPayeeFromPaymentResponse(registerPayeeRequest));
    }

    private TransferExecutionException createInvalidDestinationAccountException() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        catalog.getString("You can only make payments to Swedish destinations"))
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    /**
     * Returns a function that streams through all registered payees with a filter to find the newly
     * added payee among them.
     */
    private Function<PaymentBaseinfoResponse, Optional<AbstractAccountEntity>>
            findNewPayeeFromPaymentResponse(RegisterPayeeRequest newPayee) {

        String newPayeeType = newPayee.getType().toLowerCase();
        String newPayeeAccountNumber =
                newPayee.getAccountNumber().replaceAll(INVALID_CHARACTERS, ReturnValue.EMPTY);

        return confirmResponse ->
                confirmResponse.getPayment().getPayees().stream()
                        .filter(payee -> isNewPayee(newPayeeType, newPayeeAccountNumber, payee))
                        .findFirst()
                        .map(AbstractAccountEntity.class::cast);
    }

    private boolean isNewPayee(
            String newPayeeType, String newPayeeAccountNumber, PayeeEntity payee) {
        return payee.getType().equalsIgnoreCase(newPayeeType)
                && payee.getAccountNumber()
                        .replaceAll(INVALID_CHARACTERS, ReturnValue.EMPTY)
                        .equals(newPayeeAccountNumber);
    }
}
