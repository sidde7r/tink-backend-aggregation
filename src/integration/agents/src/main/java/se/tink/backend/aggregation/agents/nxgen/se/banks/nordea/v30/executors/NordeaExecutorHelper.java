package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors;

import static com.google.common.base.Predicates.not;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.RecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.rpc.FetchEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaExecutorHelper {

    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();
    private final AgentContext context;
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final NordeaSEApiClient apiClient;

    public NordeaExecutorHelper(
            AgentContext context,
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            NordeaSEApiClient apiClient) {
        this.context = context;
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
    }

    /** Check if source account is a valid one to make a transfer or payment from. */
    protected AccountEntity validateSourceAccount(
            Transfer transfer, FetchAccountResponse accountResponse, boolean isPayment) {
        final List<AccountEntity> accounts = accountResponse.getAccounts();

        if (isPayment) {
            return accounts.stream()
                    .filter(this::isCanPayPgbgFromAccount)
                    .filter(account -> isSourceEqualsTransfer(transfer, account))
                    .findFirst()
                    .orElseThrow(this::throwInvalidSourceAccountError);
        } else {
            return accounts.stream()
                    .filter(this::isCanTransferFromAccount)
                    .filter(account -> isSourceEqualsTransfer(transfer, account))
                    .findFirst()
                    .orElseThrow(this::throwInvalidSourceAccountError);
        }
    }

    /** Check if payment destination account exist as unconfirmed among user's beneificiaries, */
    protected Optional<BeneficiariesEntity> validateDestinationAccount(Transfer transfer) {
        return apiClient.fetchBeneficiaries().getBeneficiaries().stream()
                .filter(BeneficiariesEntity::isPgOrBg)
                .filter(
                        beneficiary ->
                                isDestEqualsTransfer(transfer, beneficiary.getAccountNumber()))
                .findFirst();
    }

    /** Check if transfer destination account is an internal account. */
    protected Optional<AccountEntity> validateOwnDestinationAccount(
            Transfer transfer, FetchAccountResponse accountResponse) {
        return accountResponse.getAccounts().stream()
                .filter(account -> account.getPermissions().isCanTransferToAccount())
                .filter(
                        account ->
                                isDestEqualsTransfer(transfer, account.getTransferAccountNumber()))
                .findFirst();
    }

    private boolean isSourceEqualsTransfer(Transfer transfer, AccountEntity account) {
        return transfer.getSource()
                .getIdentifier(DEFAULT_FORMATTER)
                .equals(account.getTransferAccountNumber());
    }

    private boolean isDestEqualsTransfer(Transfer transfer, String destination) {
        return transfer.getDestination().getIdentifier(DEFAULT_FORMATTER).equals(destination);
    }

    private boolean isCanTransferFromAccount(AccountEntity accountEntity) {
        return accountEntity.getPermissions().isCanTransferFromAccount();
    }

    private boolean isCanPayPgbgFromAccount(AccountEntity accountEntity) {
        return accountEntity.getPermissions().isCanPayPgbgFromAccount();
    }

    protected Optional<BeneficiariesEntity> createRecipient(Transfer transfer) {
        AccountIdentifier destination = transfer.getDestination();

        RecipientRequest recipientRequest = new RecipientRequest();
        recipientRequest.setPaymentType(getPaymentType(destination));
        recipientRequest.setAccountNumber(
                transfer.getDestination().getIdentifier(DEFAULT_FORMATTER));
        recipientRequest.setName(findDestinationNameFor(destination));
        recipientRequest.setAccountNumberType(getPaymentAccountType(destination));

        apiClient.registerRecipient(recipientRequest);

        return validateDestinationAccount(transfer);
    }

    protected String getPaymentType(final AccountIdentifier destination) {
        if (!destination.is(AccountIdentifier.Type.SE_PG)
                && !destination.is(AccountIdentifier.Type.SE_BG)) {
            throwInvalidPaymentType();
        }
        return destination.is(AccountIdentifier.Type.SE_PG)
                ? NordeaSEConstants.PaymentTypes.PLUSGIRO
                : NordeaSEConstants.PaymentTypes.BANKGIRO;
    }

    protected String getPaymentAccountType(final AccountIdentifier destination) {
        return destination.is(AccountIdentifier.Type.SE_PG)
                ? NordeaSEConstants.PaymentAccountTypes.PLUSGIRO
                : NordeaSEConstants.PaymentAccountTypes.BANKGIRO;
    }

    /**
     * Try to get destination name from destination. Otherwise ask user for destination name via a
     * supplemental information.
     */
    private String findDestinationNameFor(final AccountIdentifier destination) {
        Optional<String> destinationName = destination.getName();

        return destinationName.orElseGet(this::askUserForDestinationName);
    }

    private String askUserForDestinationName() {
        try {
            Map<String, String> nameResponse =
                    supplementalInformationHelper.askSupplementalInformation(getNameInputField());
            String destinationName =
                    nameResponse.get(NordeaSEConstants.Transfer.RECIPIENT_NAME_FIELD_NAME);

            if (!Strings.isNullOrEmpty(destinationName)) {
                return destinationName;
            }

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            context.getCatalog()
                                    .getString(NordeaSEConstants.LogMessages.NO_RECIPIENT_NAME))
                    .build();

        } catch (SupplementalInfoException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            context.getCatalog()
                                    .getString(NordeaSEConstants.LogMessages.NO_RECIPIENT_NAME))
                    .build();
        }
    }

    private Field getNameInputField() {
        Field nameField = new Field();
        nameField.setDescription(NordeaSEConstants.Transfer.RECIPIENT_NAME_FIELD_DESCRIPTION);
        nameField.setName(NordeaSEConstants.Transfer.RECIPIENT_NAME_FIELD_NAME);

        return nameField;
    }

    public void confirm(ConfirmTransferRequest confirmTransferRequest, String id) {
        try {
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
                throwTransferError();
            }
        }
    }

    public void sign(SignatureRequest signatureRequest, String transferId) {
        SignatureResponse signatureResponse = apiClient.signTransfer(signatureRequest);

        if (signatureResponse.getSignatureState().equals(BankIdStatus.WAITING)) {
            pollSignTransfer(transferId, signatureResponse.getOrderReference());
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
                        throwBankIdAlreadyInProgressError();
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
                ResultSignResponse signResponse = apiClient.pollSignTransfer(orderRef, i);
                status = signResponse.getBankIdStatus();

                switch (status) {
                    case DONE:
                        completeTransfer(orderRef);
                        return;
                    case WAITING:
                        break;
                    case CANCELLED:
                        throwBankIdCancelledError();
                    default:
                        throwSignTransferFailedError();
                }
                Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == HttpStatus.SC_CONFLICT) {
                    throwBankIdAlreadyInProgressError();
                }
            }
        }
    }

    /** Check the transfer did not receive status rejected in the outbox */
    private void assertSuccessfulSign(String transferId) {
        FetchEInvoiceResponse fetchOutbox = apiClient.fetchEInvoice();

        Optional<EInvoiceEntity> rejectedTransfer =
                fetchOutbox.getEInvoices().stream()
                        .filter(entity -> entity.getId().equalsIgnoreCase(transferId))
                        .filter(EInvoiceEntity::isRejected)
                        .findFirst();

        if (rejectedTransfer.isPresent()) {
            throwTransferRejectedError();
        }
    }

    /**
     * Returns true if the registered transfer (which we got a fail response for) does not exist in
     * the outbox.
     */
    private boolean isTransferFailedButWasSuccessful(String transferId) {
        FetchEInvoiceResponse fetchOutbox = apiClient.fetchEInvoice();

        Optional<EInvoiceEntity> failedTransfer =
                fetchOutbox.getEInvoices().stream()
                        .filter(not(EInvoiceEntity::isConfirmed))
                        .filter(
                                EInvoiceEntity
                                        ::isNotPlusgiro) // plusgiro does not have any id-field
                        .filter(entity -> entity.getId().equalsIgnoreCase(transferId))
                        .findFirst();

        return !failedTransfer.isPresent();
    }

    protected void throwInvalidDestError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.INVALID_DESTINATION))
                .build();
    }

    protected TransferExecutionException throwFailedFetchAccountsError()
            throws TransferExecutionException {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.UNABLE_TO_FETCH_ACCOUNTS)
                .build();
    }

    protected void throwPaymentFailedError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.PAYMENT_ERROR)
                .build();
    }

    private TransferExecutionException throwInvalidSourceAccountError()
            throws TransferExecutionException {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                .build();
    }

    private void throwInvalidPaymentType() {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(
                        catalog.getString("You can only make payments to Swedish destinations"))
                .build();
    }

    private void throwBankIdAlreadyInProgressError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
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

    private void throwBankIdCancelledError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_CANCELLED.getKey().get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_CANCELLED))
                .build();
    }

    private void throwSignTransferFailedError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED
                                .getKey()
                                .get())
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED))
                .build();
    }

    private void throwTransferRejectedError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.TRANSFER_REJECTED)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.TRANSFER_REJECTED)
                .build();
    }

    protected void throwTransferFailedError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        this.catalog.getString(
                                TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED))
                .build();
    }

    public TransferExecutionException throwEInvoiceFailedError() throws TransferExecutionException {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.LogMessages.EINVOICE_NOT_FOUND)
                .setEndUserMessage(
                        catalog.getString(
                                TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                .build();
    }

    public void throwTransferError() {
        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(NordeaSEConstants.ErrorCodes.TRANSFER_ERROR)
                .setEndUserMessage(NordeaSEConstants.ErrorCodes.TRANSFER_ERROR)
                .build();
    }

    private void completeTransfer(String orderRef) {
        apiClient.completeTransfer(orderRef);
    }
}
