package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.AbstractBankIdSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.common.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

public class SwedbankTransferHelper {
    public enum ReferenceType {
        OCR, MESSAGE
    }

    private static final int MAX_ATTEMPTS = 90;

    private SwedbankDefaultApiClient apiClient;

    public SwedbankTransferHelper(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public LinksEntity collectBankId(AbstractBankIdSignResponse bankIdSignResponse) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String signingStatus = bankIdSignResponse.getSigningStatus();
            if (Strings.isNullOrEmpty(signingStatus)) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED).build();
            }

            switch (signingStatus) {
            case SwedbankBaseConstants.BankIdStatus.USER_SIGN:
                break;
            case SwedbankBaseConstants.BankIdStatus.COMPLETE:
                return bankIdSignResponse.getLinks();
            default:
                throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_TRANSFER_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED).build();
            }

            LinksEntity links = bankIdSignResponse.getLinks();
            if (links == null) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage(TransferExecutionException.EndUserMessage.TRANSFER_CONFIRM_FAILED)
                        .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_FAILED).build();
            }

            bankIdSignResponse = apiClient.collectSignBankId(links.getNextOrThrow());
            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE)
                .setMessage(SwedbankBaseConstants.ErrorMessage.COLLECT_BANKID_CANCELLED).build();
    }

    public static ReferenceType getReferenceTypeFor(Transfer transfer) {
        GiroMessageValidator giroValidator = GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr = giroValidator.validate(transfer.getDestinationMessage()).getValidOcr();

        return validOcr.isPresent() ? ReferenceType.OCR : ReferenceType.MESSAGE;
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
