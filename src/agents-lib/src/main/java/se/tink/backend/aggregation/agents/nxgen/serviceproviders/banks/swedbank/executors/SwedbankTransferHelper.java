package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.AbstractBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

public class SwedbankTransferHelper {

    private SwedbankDefaultApiClient apiClient;

    public SwedbankTransferHelper(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public LinksEntity collectBankId(AbstractBankIdSignResponse bankIdSignResponse) {
        for (int i = 0; i < SwedbankBaseConstants.BankId.MAX_ATTEMPTS; i++) {
            SwedbankBaseConstants.BankIdResponseStatus signingStatus = bankIdSignResponse.getBankIdStatus();

            switch (signingStatus) {
                case CLIENT_NOT_STARTED:
                case USER_SIGN:
                    break;
                case COMPLETE:
                    return bankIdSignResponse.getLinks();
                case CANCELLED:
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED).build();
                case TIMEOUT:
                default:
                    throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                            .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED)
                            .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED).build();
            }

            LinksEntity links = bankIdSignResponse.getLinks();
            if (links == null) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED).build();
            }

            bankIdSignResponse = apiClient.collectSignBankId(links.getNextOrThrow());
            Uninterruptibles.sleepUninterruptibly(SwedbankBaseConstants.BankId.BANKID_SLEEP_INTERVAL, TimeUnit.MILLISECONDS);
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE)
                .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED).build();
    }

    public static SwedbankBaseConstants.ReferenceType getReferenceTypeFor(Transfer transfer) {
        GiroMessageValidator giroValidator = GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr = giroValidator.validate(transfer.getDestinationMessage()).getValidOcr();

        return validOcr.isPresent() ? SwedbankBaseConstants.ReferenceType.OCR : SwedbankBaseConstants.ReferenceType.MESSAGE;
    }

    public static void confirmSuccessfulTransfer(ConfirmTransferResponse confirmTransferResponse, String idToConfirm) {
        if (!confirmTransferResponse.isTransferConfirmed(idToConfirm)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.TRANSFER_CONFIRM_FAILED).build();
        }
    }

    public static AccountIdentifier getDestinationAccount(Transfer transfer) {
        AccountIdentifier destinationAccount = transfer.getDestination();
        if (destinationAccount == null || !destinationAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_DESTINATION)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_DESTINATION).build();
        }

        return destinationAccount;
    }

    public static AccountIdentifier getSourceAccount(Transfer transfer) {
        AccountIdentifier sourceAccount = transfer.getSource();
        if (sourceAccount == null || !sourceAccount.isValid()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE)
                    .setMessage(SwedbankBaseConstants.ErrorMessage.INVALID_SOURCE).build();
        }

        return sourceAccount;
    }

    public static void ensureLinksNotNull(LinksEntity linksEntity,
            TransferExecutionException.EndUserMessage endUserMessage, String message) {
        if (linksEntity == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(endUserMessage)
                    .setMessage(message).build();
        }
    }
}
