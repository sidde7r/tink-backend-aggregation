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
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdBodyEntity;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;

public class IcaBankenExecutorHelper {
    private static final Logger log = LoggerFactory.getLogger(IcaBankenExecutorHelper.class);

    private IcaBankenApiClient apiClient;
    private final AgentContext context;
    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public IcaBankenExecutorHelper(IcaBankenApiClient apiClient, AgentContext context, Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.context = context;
        this.supplementalRequester = context;
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public AccountEntity findSourceAccount(final AccountIdentifier source, Collection<AccountEntity> accounts) {
        Optional<AccountEntity> fromAccount = IcaBankenExecutorUtils.tryFindOwnAccount(source, accounts);

        return fromAccount.orElseThrow(() ->
                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(context.getCatalog().getString(
                                TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                        .build());
    }

    /**
     * First try to find the destination among user's save recipients. If not present try to add a new recipient.
     * If adding a new recipient fails throws an exception as the destination is invalid.
     */
    public RecipientEntity findDestinationAccount(final AccountIdentifier destination) {

        return getRecipientEntity(destination, apiClient.fetchDestinationAccounts())
                .orElseGet(() -> addNewRecipient(destination)
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(context.getCatalog().getString(
                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
                        .build()));
    }

    /**
     * Add new recipient to user's list of recipients, after add, search for newly added recipient in list of
     * user's recipients. Return result of that search.
     */
    private Optional<RecipientEntity> addNewRecipient(final AccountIdentifier destination) {
        String recipientType = IcaBankenExecutorUtils.getRecipientType(destination.getType(), context.getCatalog());

        // Create the new recipient.
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setAccountNumber(destination.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER));
        recipientEntity.setType(recipientType);
        recipientEntity.setName(findDestinationNameFor(destination));
        recipientEntity.setBudgetGroup("");

        if (AccountIdentifier.Type.SE.equals(destination.getType())) {
            recipientEntity.setTransferBankId(fetchTransferBankIdFor(destination));
        }

        apiClient.saveNewRecipient(recipientEntity);

        return getRecipientEntity(destination, apiClient.fetchDestinationAccounts());
    }

    /**
     * Search for bank account destination or payment account (BG || PG) destination in list of user's saved
     * recipients and returns result of that search.
     */

    private Optional<RecipientEntity> getRecipientEntity(AccountIdentifier destination,
            List<RecipientEntity> destinationAccounts) {

        if (AccountIdentifier.Type.SE.equals(destination.getType())) {
            return IcaBankenExecutorUtils.tryFindRegisteredTransferAccount(destination,  destinationAccounts);
        }

        return IcaBankenExecutorUtils.tryFindRegisteredPaymentAccount(destination, destinationAccounts);
    }

    /**
     * Try to get destination name from destination. If not present try to fetch payment (BG || PG) destination
     * names through an API call, otherwise ask user for destination name via a supplemental information.
     */
    private String findDestinationNameFor(final AccountIdentifier destination) {
        Optional<String> destinationName = destination.getName();

        if (destinationName.isPresent()) {
            return destinationName.get();
        }

        if (!AccountIdentifier.Type.SE.equals(destination.getType())) {
            destinationName = apiClient.fetchPaymentDestinationName(
                    destination.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER));
        }

        return destinationName.orElseGet(this::askUserForDestinationName);
    }

    private String askUserForDestinationName() {
        try {
            Map<String, String> nameResponse = supplementalInformationHelper
                    .askSupplementalInformation(getNameInputField());
            String destinationName = nameResponse.get(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_NAME);

            if (!Strings.isNullOrEmpty(destinationName)) {
                return destinationName;
            }

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(context.getCatalog().getString(IcaBankenConstants.LogMessage.NO_RECIPIENT_NAME))
                    .build();
        } catch (SupplementalInfoException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(context.getCatalog().getString(IcaBankenConstants.LogMessage.NO_RECIPIENT_NAME))
                    .build();
        }
    }

    private Field getNameInputField() {
        Field nameField = new Field();
        nameField.setDescription(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_DESCRIPTION);
        nameField.setName(IcaBankenConstants.Transfers.RECIPIENT_NAME_FIELD_NAME);

        return nameField;
    }

    /**
     * Fetch transfer banks from the bank and try to return the one that matches the clearing number of the
     * destination account. E.g. Swedbank has the bank id 8000, this method would then return 8000 for a destination
     * with 8327 as clearing number.
     *
     * Throws an exception if no match is found as a valid transfer bank could not be found for the given destination.
     */
    private String fetchTransferBankIdFor(final AccountIdentifier destination) {
        List<TransferBankEntity> transferBankEntities = apiClient.fetchTransferBanks();

        Optional<TransferBankEntity> transferBankEntity = IcaBankenExecutorUtils.findBankForAccountNumber(
                destination.getIdentifier(IcaBankenFormatUtils.DEFAULT_FORMATTER), transferBankEntities);

        if (transferBankEntity.isPresent()) {
            return transferBankEntity.get().getTransferBankId();
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(context.getCatalog().getString(
                        "Could not find a bank for the given destination account. Check the account number and try again."))
                .build();
    }

    public void signTransfer(Transfer transfer, AccountEntity sourceAccount) {
        try {
            BankIdBodyEntity bankIdResponse = apiClient.initTransferSign();
            String reference = bankIdResponse.getRequestId();

            supplementalRequester.openBankId(bankIdResponse.getAutostartToken(), false);
            poll(reference);
            assertSuccessfulSign(reference);
        } catch (Exception initialException) {
            cleanUpOutbox();

            if (!isTransferFailedButWasSuccessful(transfer, sourceAccount)) {
                if (initialException.getCause() instanceof HttpResponseException) {
                    HttpResponse response = ((HttpResponseException) initialException.getCause()).getResponse();
                    if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                        throwBankIdAlreadyInProgressError();
                    }
                }

                throw initialException;
            }
        }
    }

    public String signEInvoice(String invoiceId) {
        BankIdBodyEntity bankIdResponse = apiClient.initEInvoiceBankId(invoiceId);
        String reference = bankIdResponse.getRequestId();

        supplementalRequester.openBankId(bankIdResponse.getAutostartToken(), false);
        poll(reference);

        return reference;
    }

    private void throwBankIdAlreadyInProgressError() {

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS.getKey().get())
                .setEndUserMessage(catalog.getString(
                        TransferExecutionException.EndUserMessage.BANKID_ANOTHER_IN_PROGRESS))
                .build();
    }

    private void cleanUpOutbox() {
        try {
            List<AssignmentEntity> unsignedTransfers = apiClient.fetchUnsignedTransfers();
            deleteUnsignedTransfer(unsignedTransfers);
        } catch (Exception deleteException) {
            log.warn("Could not delete transfer in outbox. "
                            + "If unsigned transfers are left here, user could end up in a deadlock.",
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
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setMessage(TransferExecutionException.EndUserMessage.BANKID_CANCELLED.getKey().get())
                            .setEndUserMessage(catalog.getString(
                                    TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                            .build();
                default:
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setMessage(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED.getKey().get())
                            .setEndUserMessage(catalog.getString(
                                    TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED))
                            .build();
                }

                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);

            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setMessage(IcaBankenConstants.UserMessage.BANKID_TRANSFER_INTERRUPTED.getKey().get())
                            .setEndUserMessage(catalog.getString(
                                    IcaBankenConstants.UserMessage.BANKID_TRANSFER_INTERRUPTED))
                            .build();
                }
            }

        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE.getKey().get())
                .setEndUserMessage(catalog.getString(
                        TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE))
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

    private void deleteUnsignedTransfer(List<AssignmentEntity> unsignedTransfers) {
        if (unsignedTransfers.size() == 1) {
            String transferId = unsignedTransfers.get(0).getRegistrationId();
            apiClient.deleteUnsignedTransfer(transferId);
        }
    }

    /**
     * Returns true if the registered transfer (which we got a fail response for) exists in the list
     * of upcoming transactions.
     */
    private boolean isTransferFailedButWasSuccessful(Transfer transfer, AccountEntity sourceAccount) {

        List<UpcomingTransactionEntity> sourceAccountUpcomingTransactions = apiClient.fetchUpcomingTransactions()
                .stream()
                .filter(upcomingTransaction -> upcomingTransaction.belongsTo(sourceAccount.getAccountId()))
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
                    .setMessage(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS.getKey().get())
                    .setEndUserMessage(catalog.getString(
                            TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS))
                    .build();
        }
    }

    public boolean hasUnsignedTransfers() {
        return !apiClient.fetchUnsignedTransfers().isEmpty();
    }

    /**
     * Try to put transfer in user's outbox. If the date is not a bank day ICA Banken returns a 409 response with a
     * new suggested date. Will then update the date and try to add to outbox again.
     */
    public void putTransferInOutbox(TransferRequest transferRequest) {

        try {
            apiClient.putAssignmentInOutbox(transferRequest);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            // Conflict (409) means the date was a non bank day, update transfer request with suggested date.
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                TransferResponse transferResponse = response.getBody(TransferResponse.class);

                if (transferResponse.getBody().getProposedNewDate() != null) {
                    transferRequest.setDueDate(transferResponse.getBody().getProposedNewDate());
                    transferResponse = apiClient.putAssignmentInOutbox(transferRequest);
                }

                if (transferResponse.getResponseStatus().getCode() != IcaBankenConstants.StatusCodes.OK_RESPONSE) {
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(getEndUserMessage(transferResponse,
                                    TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED))
                            .build();
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * Attempts to get a detailed error message from the bank. If not present, it takes the more general alternative.
     */
    private String getEndUserMessage(BaseResponse errorResponse,
            TransferExecutionException.EndUserMessage generalErrorMessage) {
        String message = errorResponse.getResponseStatus().getClientMessage();

        if (Strings.isNullOrEmpty(message)) {
            message = context.getCatalog().getString(generalErrorMessage);
        }

        return message;
    }
}
