package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.ApproveEInvoiceRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.ApproveEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.EInvoiceDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_AMOUNT;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DESTINATION_MESSAGE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_DUEDATE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_FAILED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_NOT_ALLOWED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_SOURCE_MESSAGE;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_MULTIPLE_MATCHES;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED;

public class HandelsbankenSEEInvoiceExecutor implements ApproveEInvoiceExecutor {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSEEInvoiceExecutor(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void approveEInvoice(Transfer transfer) throws TransferExecutionException {
        EInvoice eInvoice = fetchEInvoice(transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID)
                .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED)));

        EInvoiceDetails eInvoiceDetails = updateIfChanged(transfer, client.eInvoiceDetails(eInvoice)
                .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED)));

        signEInvoice(eInvoiceDetails);
    }

    private EInvoice fetchEInvoice(String approvalId) {
        List<EInvoice> matchingEInvoices = sessionStorage.applicationEntryPoint().map(client::pendingEInvoices)
                .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED))
                .getEinvoiceStream()
                .filter(eInvoice -> eInvoice.getApprovalId().equals(approvalId))
                .collect(Collectors.toList());

        if (matchingEInvoices.isEmpty()) {
            throw exception(EINVOICE_NO_MATCHES);
        } else if (matchingEInvoices.size() > 1) {
            throw exception(EINVOICE_MULTIPLE_MATCHES);
        }
        return matchingEInvoices.get(0);
    }

    private EInvoiceDetails updateIfChanged(Transfer transfer, EInvoiceDetails eInvoiceDetails) {
        try {
            eInvoiceDetails = (EInvoiceDetails) new HandelsbankenSEPaymentExecutor(client, sessionStorage)
                    .updateIfChanged(transfer, eInvoiceDetails);
        } catch (TransferExecutionException e) {
            throw translateUpdateException(e);
        }
        return eInvoiceDetails;
    }

    private void signEInvoice(EInvoiceDetails eInvoiceDetails) {
        if (eInvoiceDetails.getApprovalId() == null) {
            throw exception(EINVOICE_MODIFY_FAILED);
        }
        ApproveEInvoiceResponse approveEInvoiceResponse = client
                .approveEInvoice(eInvoiceDetails, ApproveEInvoiceRequest.create(eInvoiceDetails.getApprovalId()))
                .orElseThrow(() -> exception(EINVOICE_MODIFY_FAILED));

        client.signEInvoice(approveEInvoiceResponse);
    }

    private TransferExecutionException translateUpdateException(TransferExecutionException e) {
        switch (EndUserMessage.valueOf(e.getUserMessage())) {
        case PAYMENT_UPDATE_NOT_ALLOWED:
            throw exception(EINVOICE_MODIFY_NOT_ALLOWED);
        case PAYMENT_UPDATE_AMOUNT:
            throw exception(EINVOICE_MODIFY_AMOUNT);
        case PAYMENT_UPDATE_DUEDATE:
            throw exception(EINVOICE_MODIFY_DUEDATE);
        case PAYMENT_UPDATE_DESTINATION_MESSAGE:
            throw exception(EINVOICE_MODIFY_DESTINATION_MESSAGE);
        case PAYMENT_UPDATE_SOURCE:
            throw exception(EINVOICE_MODIFY_SOURCE);
        case PAYMENT_UPDATE_SOURCE_MESSAGE:
            throw exception(EINVOICE_MODIFY_SOURCE_MESSAGE);
        case PAYMENT_UPDATE_DESTINATION:
            throw exception(EINVOICE_MODIFY_DESTINATION);
        default:
            return exception(EINVOICE_MODIFY_FAILED);
        }
    }

    // A lot of exceptions are thrown in this executor, this method saves us a lot of lines
    private TransferExecutionException exception(EndUserMessage endUserMessage) {
        return TransferExecutionException
                .builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.toString())
                .build();
    }
}
