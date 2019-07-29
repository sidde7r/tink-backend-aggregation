package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaExecutorHelper {
    // TODO extend BankIdSignHelper
    private static final NordeaAccountIdentifierFormatter NORDEA_ACCOUNT_FORMATTER =
            new NordeaAccountIdentifierFormatter();
    private final AgentContext context;
    private final Catalog catalog;
    private final NordeaSEApiClient apiClient;

    public NordeaExecutorHelper(
            AgentContext context, Catalog catalog, NordeaSEApiClient apiClient) {
        this.context = context;
        this.catalog = catalog;
        this.apiClient = apiClient;
    }

    /** Check if source account is a valid one to make a transfer or payment from. */
    protected AccountEntity validateSourceAccount(
            Transfer transfer, FetchAccountResponse accountResponse, boolean isPayment) {
        final List<AccountEntity> accounts = accountResponse.getAccounts();

        if (isPayment) {
            return accounts.stream()
                    .filter(this::isCanPayPgbgFromAccount)
                    .filter(
                            account ->
                                    isAccountIdentifierEquals(
                                            transfer.getSource(), account.getAccountIdentifier()))
                    .findFirst()
                    .orElseThrow(this::invalidSourceAccountError);
        } else {
            return accounts.stream()
                    .filter(this::isCanTransferFromAccount)
                    .filter(
                            account ->
                                    isAccountIdentifierEquals(
                                            transfer.getSource(), account.getAccountIdentifier()))
                    .findFirst()
                    .orElseThrow(this::invalidSourceAccountError);
        }
    }

    /** Check if payment destination account exist as unconfirmed among user's beneificiaries, */
    protected Optional<BeneficiariesEntity> validateDestinationAccount(Transfer transfer) {
        return apiClient.fetchBeneficiaries().getBeneficiaries().stream()
                .filter(
                        beneficiary ->
                                isAccountIdentifierEquals(
                                        transfer.getDestination(),
                                        beneficiary.generalGetAccountIdentifier()))
                .findFirst();
    }

    /** Check if transfer destination account is an internal account. */
    protected Optional<AccountEntity> validateOwnDestinationAccount(
            Transfer transfer, FetchAccountResponse accountResponse) {
        return accountResponse.getAccounts().stream()
                .filter(account -> account.getPermissions().isCanTransferToAccount())
                .filter(
                        account ->
                                isAccountIdentifierEquals(
                                        transfer.getDestination(), account.getAccountIdentifier()))
                .findFirst();
    }

    private boolean isAccountIdentifierEquals(AccountIdentifier id1, AccountIdentifier id2) {
        return id1.getIdentifier(NORDEA_ACCOUNT_FORMATTER)
                .equals(id2.getIdentifier(NORDEA_ACCOUNT_FORMATTER));
    }

    private boolean isCanTransferFromAccount(AccountEntity accountEntity) {
        return accountEntity.getPermissions().isCanTransferFromAccount();
    }

    private boolean isCanPayPgbgFromAccount(AccountEntity accountEntity) {
        return accountEntity.getPermissions().isCanPayPgbgFromAccount();
    }

    public String getPaymentType(final AccountIdentifier destination) {
        if (!destination.is(AccountIdentifier.Type.SE_PG)
                && !destination.is(AccountIdentifier.Type.SE_BG)) {
            throw invalidPaymentType();
        }
        return destination.is(AccountIdentifier.Type.SE_PG)
                ? NordeaSEConstants.PaymentTypes.PLUSGIRO
                : NordeaSEConstants.PaymentTypes.BANKGIRO;
    }

    public String getPaymentAccountType(final AccountIdentifier destination) {
        return destination.is(AccountIdentifier.Type.SE_PG)
                ? NordeaSEConstants.PaymentAccountTypes.PLUSGIRO
                : NordeaSEConstants.PaymentAccountTypes.BANKGIRO;
    }

    public void confirm(String id) {
        try {
            ConfirmTransferRequest confirmTransferRequest = new ConfirmTransferRequest(id);
            ConfirmTransferResponse confirmTransferResponse =
                    apiClient.confirmBankTransfer(confirmTransferRequest);

            SignatureRequest signatureRequest = new SignatureRequest();
            SignatureEntity signatureEntity =
                    new SignatureEntity(confirmTransferResponse.getResult());
            signatureRequest.add(signatureEntity);
            // sign external transfer or einvoice
            sign(signatureRequest, id);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                throw transferError();
            }
        }
    }

    public void sign(SignatureRequest signatureRequest, String transferId) {
        SignatureResponse signatureResponse = apiClient.signTransfer(signatureRequest);
        if (signatureResponse.getSignatureState().equals(BankIdStatus.WAITING)) {
            context.openBankId(null, false);
            pollSignTransfer(transferId, signatureResponse.getOrderReference());
        } else {
            throw paymentFailedError();
        }
    }

    private void pollSignTransfer(String transferId, String orderRef) {
        try {
            poll(orderRef);
            assertSuccessfulSign(transferId);
        } catch (Exception initialException) {
            if (!isTransferFailedButWasSuccessful(transferId)) {
                if (initialException.getCause() instanceof HttpResponseException) {
                    HttpResponse response =
                            ((HttpResponseException) initialException.getCause()).getResponse();
                    if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                        throw bankIdAlreadyInProgressError();
                    }
                }

                throw initialException;
            }
        }
    }

    private void poll(String orderRef) {
        BankIdStatus status;
        for (int i = 1; i < NordeaSEConstants.Transfer.MAX_POLL_ATTEMPTS; i++) {
            try {
                ResultSignResponse signResponse = apiClient.pollSign(orderRef, i);
                status = signResponse.getBankIdStatus();

                switch (status) {
                    case DONE:
                        completeTransfer(orderRef);
                        return;
                    case WAITING:
                        break;
                    case CANCELLED:
                        throw bankIdCancelledError();
                    default:
                        throw signTransferFailedError();
                }
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throw bankIdAlreadyInProgressError();
                }
            }
        }
    }

    /** Check the transfer did not receive status rejected in the outbox */
    private void assertSuccessfulSign(String transferId) {
        FetchPaymentsResponse fetchOutbox = apiClient.fetchPayments();

        Optional<PaymentEntity> rejectedTransfer =
                fetchOutbox.getPayments().stream()
                        .filter(entity -> entity.getApiIdentifier().equalsIgnoreCase(transferId))
                        .filter(PaymentEntity::isRejected)
                        .findFirst();

        if (rejectedTransfer.isPresent()) {
            throw transferRejectedError();
        }
    }

    /**
     * Returns true if the registered transfer (which we got a fail response for) does not exist in
     * the outbox.
     */
    private boolean isTransferFailedButWasSuccessful(String transferId) {
        FetchPaymentsResponse fetchOutbox = apiClient.fetchPayments();

        Optional<PaymentEntity> failedTransfer =
                fetchOutbox.getPayments().stream()
                        .filter(PaymentEntity::isUnconfirmed)
                        .filter(entity -> entity.getApiIdentifier().equalsIgnoreCase(transferId))
                        .findFirst();

        return !failedTransfer.isPresent();
    }

    /**
     * Check if payment already exist in outbox as unconfirmed if it does then return the existing
     * payment entity
     */
    protected Optional<PaymentEntity> findInOutbox(Transfer transfer) {
        return apiClient.fetchPayments().getPayments().stream()
                .filter(PaymentEntity::isUnconfirmed)
                .map(
                        paymentEntity ->
                                apiClient.fetchPaymentDetails(paymentEntity.getApiIdentifier()))
                .filter(paymentEntity -> paymentEntity.isEqualToTransfer(transfer))
                .findFirst();
    }

    protected TransferExecutionException invalidDestError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
                .build();
    }

    protected TransferExecutionException failedFetchAccountsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.UNABLE_TO_FETCH_ACCOUNTS)
                .build();
    }

    protected TransferExecutionException paymentFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .build();
    }

    private TransferExecutionException invalidSourceAccountError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                .build();
    }

    private TransferExecutionException invalidPaymentType() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        catalog.getString("You can only make payments to Swedish destinations"))
                .build();
    }

    private TransferExecutionException bankIdAlreadyInProgressError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS
                                .getKey()
                                .get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage
                                        .BANKID_ANOTHER_IN_PROGRESS))
                .build();
    }

    private TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                .build();
    }

    private TransferExecutionException signTransferFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED
                                .getKey()
                                .get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED))
                .build();
    }

    private TransferExecutionException transferRejectedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.TRANSFER_REJECTED)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.TRANSFER_REJECTED)
                .build();
    }

    protected TransferExecutionException transferFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED))
                .build();
    }

    public TransferExecutionException eInvoiceFailedError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_NOT_FOUND)
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                .build();
    }

    public TransferExecutionException eInvoiceUpdateAmountNotAllowed() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_MODIFY_AMOUNT)
                .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_MODIFY_AMOUNT))
                .build();
    }

    public TransferExecutionException eInvoiceUpdateMessageNotAllowed() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_MODIFY_DESTINATION_MESSAGE)
                .setEndUserMessage(
                        catalog.getString(EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE))
                .build();
    }

    public TransferExecutionException eInvoiceUpdateDueNotAllowed() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_MODIFY_DUEDATE)
                .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_MODIFY_DUEDATE))
                .build();
    }

    public TransferExecutionException eInvoiceUpdateFromNotAllowed() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_MODIFY_SOURCE)
                .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_MODIFY_SOURCE))
                .build();
    }

    public TransferExecutionException eInvoiceUpdateToNotAllowed() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_MODIFY_DESTINATION)
                .setEndUserMessage(catalog.getString(EndUserMessage.EINVOICE_MODIFY_DESTINATION))
                .build();
    }

    private TransferExecutionException transferError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.TRANSFER_ERROR)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.TRANSFER_ERROR)
                .build();
    }

    private void completeTransfer(String orderRef) {
        apiClient.completeTransfer(orderRef);
    }
}
