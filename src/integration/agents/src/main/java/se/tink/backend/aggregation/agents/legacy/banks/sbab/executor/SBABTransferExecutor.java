package se.tink.backend.aggregation.agents.banks.sbab.executor;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.agents.banks.sbab.client.TransferClient;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.TransferAccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.TransferEntity;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.InitiateSignResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.SignProcessResponse;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.TransferRequest;
import se.tink.backend.aggregation.agents.banks.sbab.executor.rpc.ValidateRecipientRequest;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class SBABTransferExecutor {
    private static final Logger log = LoggerFactory.getLogger(SBABTransferExecutor.class);

    private final TransferClient transferClient;
    private final Catalog catalog;
    private final TransferMessageFormatter messageFormatter;
    private final SupplementalRequester supplementalRequester;

    public SBABTransferExecutor(
            TransferClient transferClient,
            Catalog catalog,
            SupplementalRequester supplementalRequester) {
        this.transferClient = transferClient;
        this.catalog = catalog;

        this.messageFormatter =
                new TransferMessageFormatter(
                        catalog,
                        TransferMessageLengthConfig.createWithMaxLength(30, 12, 12),
                        new StringNormalizerSwedish("!+%\"/?,.§\\-"));
        this.supplementalRequester = supplementalRequester;
    }

    public void executeBankTransfer(Transfer transfer) {

        Optional<TransferAccountEntity> sourceAccount = transferClient.findSourceAccount(transfer);

        if (!sourceAccount.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .setEndUserMessage(
                            catalog.getString(
                                    TransferExecutionException.EndUserMessage.SOURCE_NOT_FOUND))
                    .build();
        }

        boolean isInternalTransfer = transferClient.isBetweenUserAccounts(transfer);

        if (isInternalTransfer) {
            makeInternalTransfer(transfer);
        } else {
            makeExternalTransfer(transfer);
        }
    }

    private void makeInternalTransfer(Transfer transfer) {
        TransferMessageFormatter.Messages messages =
                messageFormatter.getMessagesFromRemittanceInformation(transfer, true);

        TransferRequest transferRequest = TransferRequest.create(transfer, messages, true);
        transferClient.validateTransfer(transferRequest);

        confirmTransfer(transferRequest);
    }

    private void makeExternalTransfer(Transfer transfer) {
        TransferMessageFormatter.Messages messages =
                messageFormatter.getMessagesFromRemittanceInformation(transfer, false);

        boolean destinationIsSavedRecipient = transferClient.isSavedRecipient(transfer);

        if (!destinationIsSavedRecipient) {
            transferClient.validateRecipient(ValidateRecipientRequest.create(transfer));
        }

        TransferRequest transferRequest = TransferRequest.create(transfer, messages, false);
        transferClient.validateTransfer(transferRequest);

        SignProcessResponse signProcessResponse =
                transferClient.initiateSignProcess(transferRequest);

        signTransfer(signProcessResponse);

        TransferRequest confirmTransferRequest =
                transferRequest.setSignatureProcessResponse(signProcessResponse);

        confirmTransfer(confirmTransferRequest);
    }

    private void confirmTransfer(TransferRequest finaliseTransferRequest) {
        TransferEntity confirmedTransfer = transferClient.confirmTransfer(finaliseTransferRequest);

        if (!confirmedTransfer.hasSuccessStatus()) {

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(catalog.getString(EndUserMessage.TRANSFER_CONFIRM_FAILED))
                    .setEndUserMessage(catalog.getString(EndUserMessage.TRANSFER_CONFIRM_FAILED))
                    .build();
        }
    }

    private void signTransfer(SignProcessResponse signProcessResponse) {
        String bankIdRef = signProcessResponse.getBankIdRefOrThrowIfNotPresent(catalog);

        InitiateSignResponse initiateSignResponse = transferClient.initiateBankIdSign(bankIdRef);
        String initStatus = initiateSignResponse.getStatus();

        if (SBABConstants.BankId.ALREADY_IN_PROGRESS.equalsIgnoreCase(initStatus)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(catalog.getString(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS))
                    .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_ANOTHER_IN_PROGRESS))
                    .build();
        }

        if (!SBABConstants.BankId.STARTED.equalsIgnoreCase(initStatus)) {
            log.warn("BankID init failed with unknown bankID status: {}", initStatus);

            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(catalog.getString(EndUserMessage.BANKID_TRANSFER_FAILED))
                    .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_TRANSFER_FAILED))
                    .build();
        }

        supplementalRequester.openBankId();

        collectBankId(bankIdRef);
    }

    private void collectBankId(String bankIdRef) {
        BankIdStatus status;

        for (int i = 0; i < SBABConstants.BankId.BANKID_MAX_ATTEMPTS; i++) {

            PollBankIdResponse pollBankIdResponse = transferClient.pollBankId(bankIdRef);
            status = pollBankIdResponse.getBankIdStatus();

            switch (status) {
                case DONE:
                    return;
                case WAITING:
                    break;
                case CANCELLED:
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setMessage(catalog.getString(EndUserMessage.BANKID_CANCELLED))
                            .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_CANCELLED))
                            .build();
                case FAILED_UNKNOWN:
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setMessage(catalog.getString(EndUserMessage.BANKID_TRANSFER_FAILED))
                            .setEndUserMessage(
                                    catalog.getString(EndUserMessage.BANKID_TRANSFER_FAILED))
                            .build();
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(catalog.getString(EndUserMessage.BANKID_NO_RESPONSE))
                .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_NO_RESPONSE))
                .build();
    }
}
