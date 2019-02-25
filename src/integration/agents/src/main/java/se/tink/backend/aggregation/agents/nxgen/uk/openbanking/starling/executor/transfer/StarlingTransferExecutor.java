package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.entity.TransferStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.rpc.ExecutePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.util.PaymentSignature;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.rpc.PayeesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

import java.security.PrivateKey;
import java.util.Optional;

@JsonObject
public class StarlingTransferExecutor implements BankTransferExecutor {

    public static final String COUNTRY_CODE = "GB";
    public static final String INDIVIDUAL = "INDIVIDUAL";

    private final StarlingApiClient apiClient;
    private final String keyUid;
    private final PrivateKey privateKey;

    public StarlingTransferExecutor(
            StarlingApiClient apiClient, String keyUid, PrivateKey privateKey) {
        this.apiClient = apiClient;
        this.keyUid = keyUid;
        this.privateKey = privateKey;
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {

        Preconditions.checkNotNull(transfer, "Transfer must not be null");

        Preconditions.checkNotNull(transfer.getAmount(), "Transfer amount must not be null.");
        Preconditions.checkArgument(
                transfer.getAmount().isPositive(), "Transfer amount must be positive.");

        Preconditions.checkNotNull(transfer.getSource(), "Transfer source must not be null");
        Preconditions.checkNotNull(transfer.getDestination(), "Transfer source must not be null");

        final AccountIdentifier sourceIdentifier = transfer.getSource();
        final SortCodeIdentifier destinationIdentifier =
                toSortCodeIdentifier(transfer.getDestination())
                        .orElseThrow(
                                () ->
                                        getTransferException(
                                                "Destination must SortCodeIdentifier.",
                                                TransferExecutionException.EndUserMessage
                                                        .INVALID_DESTINATION));

        final AccountEntity sourceAccount =
                getSourceAccount(sourceIdentifier)
                        .orElseThrow(
                                () ->
                                        getTransferException(
                                                "Could not find source account.",
                                                TransferExecutionException.EndUserMessage
                                                        .SOURCE_NOT_FOUND));

        final PayeeEntity payee =
                getPayeeForAccount(destinationIdentifier)
                        .orElseThrow(
                                () ->
                                        getTransferException(
                                                "Could not find payee with the specified account.",
                                                TransferExecutionException.EndUserMessage
                                                        .INVALID_DESTINATION));

        final PaymentRecipient recipient =
                PaymentRecipient.builder()
                        .setDestinationAccount(destinationIdentifier)
                        .setPayeeName(payee.getPayeeName())
                        .setCountryCode(COUNTRY_CODE)
                        .setPayeeType(INDIVIDUAL)
                        .build();

        final ExecutePaymentRequest paymentRequest =
                ExecutePaymentRequest.builder()
                        .setExternalIdentifier(transfer.getId().toString())
                        .setPaymentRecipient(recipient)
                        .setReference(transfer.getSourceMessage())
                        .setAmount(transfer.getAmount())
                        .build();

        ExecutePaymentResponse paymentResponse =
                apiClient.executeTransfer(
                        paymentRequest,
                        PaymentSignature.builder(keyUid, privateKey),
                        sourceAccount.getAccountUid(),
                        sourceAccount.getDefaultCategory());

        TransferStatusEntity status =
                apiClient.checkTransferStatus(paymentResponse.getPaymentOrderUid());

        if (!status.isOk()) {
            throw getTransferException(
                    status.getBody(),
                    TransferExecutionException.EndUserMessage.TRANSFER_EXECUTE_FAILED);
        }

        return Optional.of(paymentResponse.getPaymentOrderUid());
    }



    private Optional<PayeeEntity> getPayeeForAccount(final SortCodeIdentifier accountIdentifier) {

        PayeesResponse payees = apiClient.fetchPayees();

        return payees.getPayees().stream()
                .filter(payee -> payee.hasAccount(accountIdentifier))
                .findAny();
    }

    private Optional<AccountEntity> getSourceAccount(final AccountIdentifier accountIdentifier) {

        return apiClient.fetchAccounts().stream()
                .filter(account -> matchIdentifier(account.getAccountUid(), accountIdentifier))
                .findFirst();
    }

    private boolean matchIdentifier(
            final String accountUid, final AccountIdentifier accountIdentifier) {

        return apiClient.fetchAccountIdentifiers(accountUid).hasIdentifier(accountIdentifier);
    }

    private static Optional<SortCodeIdentifier> toSortCodeIdentifier(final AccountIdentifier identifier) {

        if (identifier.getType() == AccountIdentifier.Type.SORT_CODE) {
            return Optional.of((SortCodeIdentifier) identifier);
        }

        return Optional.empty();
    }

    private static TransferExecutionException getTransferException(
            String msg, TransferExecutionException.EndUserMessage userMsg) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(msg)
                .setEndUserMessage(userMsg)
                .build();
    }
}
