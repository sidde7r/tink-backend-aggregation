package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities.ResultsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.CompleteTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaExecutorHelper {
    private static final Logger log = LoggerFactory.getLogger(NordeaExecutorHelper.class);

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
                .filter(Predicates.or(BeneficiariesEntity::isLBAN, BeneficiariesEntity::isPgOrBg))
                .filter(
                        beneficiary ->
                                beneficiary.generalGetAccountIdentifier().isValid()
                                        && identifierCanBeFormatted(
                                                beneficiary.generalGetAccountIdentifier()))
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

        if (!identifierCanBeFormatted(transfer.getDestination())) {
            throw invalidDestError();
        }

        // Transfer source and destination must not be the same
        if (isAccountIdentifierEquals(transfer.getDestination(), transfer.getSource())) {
            throw transferCancelledWithMessage(
                    TransferExecutionException.EndUserMessage.DESTINATION_CANT_BE_SAME_AS_SOURCE);
        }

        return accountResponse.getAccounts().stream()
                .filter(account -> account.getPermissions().isCanTransferToAccount())
                .filter(
                        account ->
                                isAccountIdentifierEquals(
                                        transfer.getDestination(), account.getAccountIdentifier()))
                .findFirst();
    }

    /**
     * Method added to handle faulty Swedbank accounts, but will also handle other cases where we
     * can't properly parse the account identifier. Used when verifying transfer's destination
     * account, if parsing fails we'll then throw a transfer execution exception. Also used when
     * matching transfer's destination account with user's stored beneficiaries, if parsing fails
     * we'll just ignore that beneficiary as a candidate.
     */
    private boolean identifierCanBeFormatted(AccountIdentifier identifier) {
        try {
            identifier.getIdentifier(NORDEA_ACCOUNT_FORMATTER);
        } catch (IllegalArgumentException e) {
            log.info("Account identifier couldn't be parsed. Reason: {}.", e.getMessage());
            return false;
        }

        return true;
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

        ConfirmTransferRequest confirmTransferRequest = new ConfirmTransferRequest(id);
        ConfirmTransferResponse confirmTransferResponse =
                apiClient.confirmBankTransfer(confirmTransferRequest);

        SignatureRequest signatureRequest = new SignatureRequest();
        SignatureEntity signatureEntity = new SignatureEntity(confirmTransferResponse.getResult());
        signatureRequest.add(signatureEntity);
        // sign external transfer or einvoice
        sign(signatureRequest, id);
    }

    public void sign(SignatureRequest signatureRequest, String transferId) {
        SignatureResponse signatureResponse = apiClient.signTransfer(signatureRequest);
        if (signatureResponse.getSignatureState().equals(BankIdStatus.WAITING)) {
            context.openBankId(null, false);
            pollSignTransfer(transferId, signatureResponse.getOrderReference());
        } else {
            throw paymentFailedError(null);
        }
    }

    private void pollSignTransfer(String transferId, String orderRef) {
        try {
            CompleteTransferResponse completeTransferResponse = poll(orderRef);
            assertSuccessfulSignOrThrow(completeTransferResponse, transferId);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                throw bankIdAlreadyInProgressError(e);
            }
            throw e;
        }
    }

    private CompleteTransferResponse poll(String orderRef) {
        BankIdStatus status;
        for (int i = 1; i < NordeaSEConstants.Transfer.MAX_POLL_ATTEMPTS; i++) {
            // sleep before so when a time out occur the bankId signing is canceled after polling
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            try {
                ResultSignResponse signResponse = apiClient.pollSign(orderRef, i);
                status = signResponse.getBankIdStatus();

                switch (status) {
                    case DONE:
                        return completeTransfer(orderRef);
                    case WAITING:
                        break;
                    case CANCELLED:
                        throw bankIdCancelledError();
                    default:
                        throw signTransferFailedError();
                }
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throw bankIdAlreadyInProgressError(e);
                }
                log.error(e.getMessage(), e);
                throw signTransferFailedError();
            }
        }
        // Time out - cancel the signing request
        cancelSign(orderRef);
        throw bankIdTimedOut();
    }

    private CompleteTransferResponse completeTransfer(String orderRef) {
        return apiClient.completeTransfer(orderRef);
    }

    private void cancelSign(String orderRef) {
        try {
            // the user will still be able to sign but this will set the status at Nordea at
            // canceled.
            apiClient.cancelSign(orderRef);
        } catch (Exception e) {
            // NOP
        }
    }

    /** Check if there are errors in the Complete Transfer Response */
    private void assertSuccessfulSignOrThrow(
            CompleteTransferResponse completeTransferResponse, String transferId) {
        if (completeTransferResponse.hasErrors()) {
            throw transferRejectedError(
                    ErrorCodes.TRANSFER_REJECTED,
                    catalog.getString(EndUserMessage.TRANSFER_REJECTED));
        }
        Optional<ResultsEntity> first =
                Optional.ofNullable(completeTransferResponse.getResults())
                        .orElse(Collections.emptyList()).stream()
                        .filter(Objects::nonNull)
                        .filter(result -> transferId.equalsIgnoreCase(result.getId()))
                        .findFirst();

        Preconditions.checkState(first.isPresent(), "Got empty complete-transfer response");
        log.info("Transfer status received from nordea: " + first.get().getStatus());
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

    private TransferExecutionException transferCancelledWithMessage(
            TransferExecutionException.EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(endUserMessage.getKey().get())
                .setEndUserMessage(this.catalog.getString(endUserMessage))
                .build();
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

    public TransferExecutionException paymentFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .setException(e)
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

    protected TransferExecutionException bankIdAlreadyInProgressError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS
                                .getKey()
                                .get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage
                                        .BANKID_ANOTHER_IN_PROGRESS))
                .setException(e)
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

    private TransferExecutionException bankIdTimedOut() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(EndUserMessage.BANKID_NO_RESPONSE))
                .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_NO_RESPONSE))
                .build();
    }

    protected TransferExecutionException notEnoughFundsError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
                .setEndUserMessage(
                        catalog.getString(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT))
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

    protected TransferExecutionException duplicatePaymentError(HttpResponseException e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(NordeaSEConstants.LogMessages.DUPLICATE_PAYMENT)
                .setException(e)
                .build();
    }

    protected TransferExecutionException transferRejectedError(
            String errorMessage, String endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(errorMessage)
                .setEndUserMessage(endUserMessage)
                .build();
    }

    protected TransferExecutionException transferFailedError(Exception e) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED))
                .setException(e)
                .build();
    }

    public TransferExecutionException eInvoiceNotFoundError() {
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
}
