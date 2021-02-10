package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.SignedAssignmentListEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.AssignmentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc.TransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils.IcaBankenFormatUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenExecutorHelper {
    private static final Logger log = LoggerFactory.getLogger(IcaBankenExecutorHelper.class);

    private IcaBankenApiClient apiClient;
    private final StatusUpdater statusUpdater;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public IcaBankenExecutorHelper(
            IcaBankenApiClient apiClient,
            CompositeAgentContext context,
            Catalog catalog,
            SupplementalInformationController supplementalInformationController) {
        this.apiClient = apiClient;
        this.statusUpdater = context;
        this.catalog = catalog;
        this.supplementalInformationController = supplementalInformationController;
    }

    public AccountEntity findSourceAccount(
            final AccountIdentifier source, Collection<AccountEntity> accounts) {
        Optional<AccountEntity> fromAccount =
                IcaBankenExecutorUtils.tryFindOwnAccount(source, accounts);

        return fromAccount.orElseThrow(
                () ->
                        TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setEndUserMessage(
                                        statusUpdater
                                                .getCatalog()
                                                .getString(
                                                        TransferExecutionException.EndUserMessage
                                                                .INVALID_SOURCE))
                                .setInternalStatus(InternalStatus.INVALID_SOURCE_ACCOUNT.toString())
                                .build());
    }

    /**
     * First try to find the destination among user's save recipients. If not present try to add a
     * new recipient. If adding a new recipient fails throws an exception as the destination is
     * invalid.
     */
    public RecipientEntity findDestinationAccount(final AccountIdentifier destination) {

        return getRecipientEntity(destination, apiClient.fetchDestinationAccounts())
                .orElseGet(
                        () ->
                                addNewRecipient(destination)
                                        .orElseThrow(
                                                () ->
                                                        TransferExecutionException.builder(
                                                                        SignableOperationStatuses
                                                                                .FAILED)
                                                                .setEndUserMessage(
                                                                        statusUpdater
                                                                                .getCatalog()
                                                                                .getString(
                                                                                        TransferExecutionException
                                                                                                .EndUserMessage
                                                                                                .INVALID_DESTINATION))
                                                                .setInternalStatus(
                                                                        InternalStatus
                                                                                .INVALID_DESTINATION_ACCOUNT
                                                                                .toString())
                                                                .build()));
    }

    /**
     * Add new recipient to user's list of recipients, after add, search for newly added recipient
     * in list of user's recipients. Return result of that search.
     */
    private Optional<RecipientEntity> addNewRecipient(final AccountIdentifier destination) {
        String recipientType =
                IcaBankenExecutorUtils.getRecipientType(
                        destination.getType(), statusUpdater.getCatalog());

        // Create the new recipient.
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setAccountNumber(
                destination.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER));
        recipientEntity.setType(recipientType);
        recipientEntity.setName(findDestinationNameFor(destination));
        recipientEntity.setBudgetGroup("");

        if (AccountIdentifier.Type.SE.equals(destination.getType())) {
            recipientEntity.setTransferBankId(fetchTransferBankIdFor(destination));
        }

        try {
            if (!Strings.isNullOrEmpty(recipientEntity.getName())) {
                apiClient.saveNewRecipient(recipientEntity);
            } else {
                constructAndThrowCancelledTransferExecutionExceptionWithMessage(
                        IcaBankenConstants.LogMessage.NO_SAVE_NEW_RECIPIENT_MESSAGE,
                        TransferExecutionException.EndUserMessage
                                .COULD_NOT_SAVE_NEW_RECIPIENT_MESSAGE,
                        InternalStatus.COULD_NOT_SAVE_NEW_RECIPIENT);
            }
        } catch (HttpResponseException e) {
            handelHttpExceptionResponse(e.getResponse());
        }

        return getRecipientEntity(destination, apiClient.fetchDestinationAccounts());
    }

    /**
     * Search for bank account destination or payment account (BG || PG) destination in list of
     * user's saved recipients and returns result of that search.
     */
    private Optional<RecipientEntity> getRecipientEntity(
            AccountIdentifier destination, List<RecipientEntity> destinationAccounts) {

        if (AccountIdentifier.Type.SE.equals(destination.getType())) {
            return IcaBankenExecutorUtils.tryFindRegisteredTransferAccount(
                    destination, destinationAccounts);
        }

        return IcaBankenExecutorUtils.tryFindRegisteredPaymentAccount(
                destination, destinationAccounts);
    }

    /**
     * Try to get destination name from destination. If not present try to fetch payment (BG || PG)
     * destination names through an API call, otherwise ask user for destination name via a
     * supplemental information.
     */
    private String findDestinationNameFor(final AccountIdentifier destination) {
        Optional<String> destinationName = destination.getName();

        if (destinationName.isPresent()) {
            return destinationName.get();
        }

        if (!AccountIdentifier.Type.SE.equals(destination.getType())) {
            destinationName =
                    apiClient.fetchPaymentDestinationName(
                            destination.getIdentifier(
                                    IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER));
        }

        return destinationName.orElseGet(this::askUserForDestinationName);
    }

    private String askUserForDestinationName() {
        try {
            Map<String, String> nameResponse =
                    supplementalInformationController.askSupplementalInformationSync(
                            getNameInputField());
            String destinationName =
                    nameResponse.get(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_NAME);

            if (!Strings.isNullOrEmpty(destinationName)) {
                return destinationName;
            }

            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            statusUpdater
                                    .getCatalog()
                                    .getString(IcaBankenConstants.LogMessage.NO_RECIPIENT_NAME))
                    .setEndUserMessage(EndUserMessage.NEW_RECIPIENT_NAME_ABSENT)
                    .setInternalStatus(InternalStatus.NEW_RECIPIENT_NAME_ABSENT.toString())
                    .build();
        } catch (SupplementalInfoException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            statusUpdater
                                    .getCatalog()
                                    .getString(IcaBankenConstants.LogMessage.NO_RECIPIENT_NAME))
                    .setEndUserMessage(EndUserMessage.NEW_RECIPIENT_NAME_ABSENT)
                    .setInternalStatus(InternalStatus.NEW_RECIPIENT_NAME_ABSENT.toString())
                    .setException(e)
                    .build();
        }
    }

    private Field getNameInputField() {
        return Field.builder()
                .description(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_DESCRIPTION)
                .name(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_NAME)
                .build();
    }

    /**
     * Fetch transfer banks from the bank and try to return the one that matches the clearing number
     * of the destination account. E.g. Swedbank has the bank id 8000, this method would then return
     * 8000 for a destination with 8327 as clearing number.
     *
     * <p>Throws an exception if no match is found as a valid transfer bank could not be found for
     * the given destination.
     */
    private String fetchTransferBankIdFor(final AccountIdentifier destination) {
        List<TransferBankEntity> transferBankEntities = apiClient.fetchTransferBanks();

        Optional<TransferBankEntity> transferBankEntity =
                IcaBankenExecutorUtils.findBankForAccountNumber(
                        destination.getIdentifier(IcaBankenFormatUtils.DEFAULT_FORMATTER),
                        transferBankEntities);

        if (transferBankEntity.isPresent()) {
            return transferBankEntity.get().getTransferBankId();
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        statusUpdater
                                .getCatalog()
                                .getString(
                                        "Could not find a bank for the given destination account. Check the account number and try again."))
                .setInternalStatus(InternalStatus.INVALID_DESTINATION_ACCOUNT.toString())
                .build();
    }

    public void signTransfer(Transfer transfer, AccountEntity sourceAccount) {
        try {
            BankIdBodyEntity bankIdResponse = apiClient.initTransferSign();
            String reference = bankIdResponse.getRequestId();

            supplementalInformationController.openMobileBankIdAsync(
                    bankIdResponse.getAutostartToken());
            poll(reference);
            assertSuccessfulSign(reference);
        } catch (Exception initialException) {
            checkForUnsigedTransfersAndCleanUpOutbox(apiClient.fetchUnsignedTransfers());

            if (!isTransferFailedButWasSuccessful(transfer, sourceAccount)) {
                if (initialException.getCause() instanceof HttpResponseException) {
                    HttpResponse response =
                            ((HttpResponseException) initialException.getCause()).getResponse();
                    if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                        throwBankIdAlreadyInProgressError(initialException);
                    }
                }

                throw initialException;
            }
        }
    }

    private void throwBankIdAlreadyInProgressError(Exception initialException) {

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS
                                .getKey()
                                .get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage
                                        .BANKID_ANOTHER_IN_PROGRESS))
                .setInternalStatus(InternalStatus.BANKID_ANOTHER_IN_PROGRESS.toString())
                .setException(initialException)
                .build();
    }

    public void checkForUnsigedTransfersAndCleanUpOutbox(List<AssignmentEntity> unsignedTransfers) {
        try {
            if (!unsignedTransfers.isEmpty()) {
                deleteUnsignedTransfers(unsignedTransfers);
            }
        } catch (Exception deleteException) {
            log.warn(
                    "Could not delete transfer in outbox. If unsigned transfers are left here, user could end up in a deadlock.",
                    deleteException);
        }
    }

    public void poll(String reference) {
        BankIdStatus status;

        for (int i = 0; i < IcaBankenConstants.Transfers.MAX_POLL_ATTEMPTS; i++) {
            try {
                status = collect(reference);

                switch (status) {
                    case DONE:
                        return;
                    case WAITING:
                        break;
                    case CANCELLED:
                        throw bankIdCancelledError();
                    case TIMEOUT:
                        throw bankIdTimeoutError();
                    default:
                        throw bankIdFailedError();
                }

                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);

            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throw bankIdInterruptedError(e);
                }
            }
        }

        throw bankIdTimeoutError();
    }

    private TransferExecutionException bankIdTimeoutError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
                .setInternalStatus(InternalStatus.BANKID_NO_RESPONSE.toString())
                .build();
    }

    private TransferExecutionException bankIdCancelledError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                .setInternalStatus(InternalStatus.BANKID_CANCELLED.toString())
                .build();
    }

    private TransferExecutionException bankIdInterruptedError(HttpResponseException e) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        IcaBankenConstants.UserMessage.BANKID_TRANSFER_INTERRUPTED.getKey().get())
                .setEndUserMessage(
                        catalog.getString(
                                IcaBankenConstants.UserMessage.BANKID_TRANSFER_INTERRUPTED))
                .setInternalStatus(InternalStatus.BANKID_ANOTHER_IN_PROGRESS.toString())
                .setException(e)
                .build();
    }

    private TransferExecutionException bankIdFailedError() {
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

    private BankIdStatus collect(String reference) {
        return apiClient.pollTransferBankId(reference).getBankIdStatus();
    }

    private void assertSuccessfulSign(String reference) {
        SignedAssignmentListEntity assignments = apiClient.getSignedAssignments(reference);

        if (assignments.containRejected()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Transfer rejected by ICA Banken")
                    .setEndUserMessage("Transfer rejected by ICA Banken")
                    .build();
        }
    }

    private void deleteUnsignedTransfers(List<AssignmentEntity> unsignedTransfers) {
        unsignedTransfers.forEach(
                unsignedTransfer ->
                        apiClient.deleteUnsignedTransfer(unsignedTransfer.getRegistrationId()));
    }

    /**
     * Returns true if the registered transfer (which we got a fail response for) exists in the list
     * of upcoming transactions.
     */
    private boolean isTransferFailedButWasSuccessful(
            Transfer transfer, AccountEntity sourceAccount) {

        List<UpcomingTransactionEntity> sourceAccountUpcomingTransactions =
                apiClient.fetchUpcomingTransactions().stream()
                        .filter(
                                upcomingTransaction ->
                                        upcomingTransaction.belongsTo(sourceAccount.getAccountId()))
                        .collect(Collectors.toList());

        for (UpcomingTransactionEntity upcomingTransaction : sourceAccountUpcomingTransactions) {
            if (IcaBankenExecutorUtils.isMatchingTransfers(transfer, upcomingTransaction)) {
                return true;
            }
        }

        return false;
    }

    public void validateNoUnsignedTransfers() {
        if (hasUnsignedTransfers()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(
                            TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS
                                    .getKey()
                                    .get())
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage
                                            .EXISTING_UNSIGNED_TRANSFERS))
                    .setInternalStatus(InternalStatus.EXISTING_UNSIGNED_TRANSFERS.toString())
                    .build();
        }
    }

    public boolean hasUnsignedTransfers() {
        return !apiClient.fetchUnsignedTransfers().isEmpty();
    }

    public void putTransferInOutbox(TransferRequest transferRequest) {
        try {
            apiClient.putAssignmentInOutbox(transferRequest);
        } catch (HttpResponseException exception) {
            HttpResponse response = exception.getResponse();
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                TransferResponse transferResponse = response.getBody(TransferResponse.class);
                if (transferResponse.getResponseStatus().isReferenceShouldBeMessageError()) {
                    try {
                        transferRequest.setReferenceType(IcaBankenConstants.Transfers.MESSAGE);
                        apiClient.putAssignmentInOutbox(transferRequest);
                    } catch (HttpResponseException hre) {
                        handleRegisterPaymentError(hre, response);
                    }
                }
            }
            handleRegisterPaymentError(exception, response);
        }
    }

    private void handleRegisterPaymentError(
            HttpResponseException exception, HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_CONFLICT) {
            TransferResponse transferResponse = response.getBody(TransferResponse.class);
            if (transferResponse.getResponseStatus().getCode()
                    != IcaBankenConstants.StatusCodes.OK_RESPONSE) {
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(
                                getEndUserMessage(
                                        transferResponse,
                                        TransferExecutionException.EndUserMessage
                                                .INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY))
                        .setInternalStatus(InternalStatus.INVALID_DUE_DATE.toString())
                        .setException(exception)
                        .build();
            }
        } else {
            throw exception;
        }
    }

    /**
     * Attempts to get a detailed error message from the bank. If not present, it takes the more
     * general alternative.
     */
    private String getEndUserMessage(
            BaseResponse errorResponse,
            TransferExecutionException.EndUserMessage generalErrorMessage) {
        String message = errorResponse.getResponseStatus().getClientMessage();

        if (Strings.isNullOrEmpty(message)) {
            message = statusUpdater.getCatalog().getString(generalErrorMessage);
        }

        return message;
    }

    private void handelHttpExceptionResponse(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_CONFLICT) {
            ResponseStatusEntity error = response.getBody(ResponseStatusEntity.class);
            if (error.getCode() == IcaBankenConstants.Error.GENERIC_ERROR_CODE
                    && error.getServerMessage()
                            .equalsIgnoreCase(
                                    IcaBankenConstants.Transfers.ERROR_SAVING_RECIPIENT)) {
                constructAndThrowCancelledTransferExecutionExceptionWithMessage(
                        IcaBankenConstants.LogMessage.NO_SAVE_NEW_RECIPIENT_MESSAGE,
                        TransferExecutionException.EndUserMessage
                                .COULD_NOT_SAVE_NEW_RECIPIENT_MESSAGE,
                        InternalStatus.COULD_NOT_SAVE_NEW_RECIPIENT);
            }
        }
    }

    private void constructAndThrowCancelledTransferExecutionExceptionWithMessage(
            String message,
            TransferExecutionException.EndUserMessage endUserMessage,
            InternalStatus internalStatus) {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage((message))
                .setEndUserMessage(statusUpdater.getCatalog().getString(endUserMessage))
                .setInternalStatus(internalStatus.toString())
                .build();
    }
}
