package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice;

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
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_SIGN_FAILED;
import static se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage.EINVOICE_VALIDATE_FAILED;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.EInvoiceDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc.EInvoiceSignRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.payment.HandelsbankenSEPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.TransferSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.rpc.PendingEInvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEEInvoiceExecutor implements ApproveEInvoiceExecutor {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final ExecutorExceptionResolver exceptionResolver;
    private final HandelsbankenSEPaymentExecutor paymentExecutor;

    public HandelsbankenSEEInvoiceExecutor(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            ExecutorExceptionResolver exceptionResolver) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.exceptionResolver = exceptionResolver;
        this.paymentExecutor =
                new HandelsbankenSEPaymentExecutor(client, sessionStorage, exceptionResolver);
    }

    @Override
    public void approveEInvoice(Transfer transfer) throws TransferExecutionException {
        PendingEInvoicesResponse allEInvoices =
                sessionStorage
                        .applicationEntryPoint()
                        .map(client::pendingEInvoices)
                        .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED));

        EInvoiceDetails eInvoiceDetails = getUpdatedEInvoiceDetails(transfer, allEInvoices);

        signEInvoice(allEInvoices, eInvoiceDetails);
    }

    private EInvoice fetchEInvoice(PendingEInvoicesResponse allEInvoices, String approvalId) {
        List<EInvoice> matchingEInvoices =
                allEInvoices
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

    private EInvoiceDetails getUpdatedEInvoiceDetails(
            Transfer transfer, PendingEInvoicesResponse allEInvoices) {
        EInvoice eInvoice =
                fetchEInvoice(
                        allEInvoices,
                        transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID)
                                .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED)));

        EInvoiceDetails eInvoiceDetails =
                client.eInvoiceDetails(eInvoice)
                        .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED));

        try {
            if (paymentExecutor.updateIfChanged(transfer, eInvoiceDetails)) {
                return fetchEInvoiceDetailsByIdInHref(
                        getIdInHref(eInvoiceDetails.toSelf().toString()));
            }

        } catch (TransferExecutionException e) {
            throw translateUpdateException(e);
        }

        return eInvoiceDetails;
    }

    // We are again getting all the E-Invoices since after update approvalId is changed by Id in get
    // Href for invoice is still same.
    private EInvoiceDetails fetchEInvoiceDetailsByIdInHref(String idInHref) {
        List<EInvoice> matchingEInvoices =
                sessionStorage
                        .applicationEntryPoint()
                        .map(client::pendingEInvoices)
                        .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED))
                        .getEinvoiceStream()
                        .filter(
                                eInvoice ->
                                        eInvoice.toEInvoiceDetails()
                                                .get()
                                                .toString()
                                                .contains(idInHref))
                        .collect(Collectors.toList());

        if (matchingEInvoices.isEmpty()) {
            throw exception(EINVOICE_NO_MATCHES);
        } else if (matchingEInvoices.size() > 1) {
            throw exception(EINVOICE_MULTIPLE_MATCHES);
        }

        return client.eInvoiceDetails(matchingEInvoices.get(0))
                .orElseThrow(() -> exception(EINVOICE_VALIDATE_FAILED));
    }

    private void signEInvoice(
            PendingEInvoicesResponse pendingEInvoices, EInvoiceDetails eInvoiceDetails) {
        if (eInvoiceDetails.getApprovalId() == null) {
            throw exception(EINVOICE_MODIFY_FAILED);
        }

        EInvoiceSignRequest signRequest =
                EInvoiceSignRequest.create(eInvoiceDetails.getApprovalId());

        TransferSignResponse approveEInvoiceResponse =
                pendingEInvoices
                        .toApproval()
                        .map(url -> client.signTransfer(url, signRequest))
                        .orElseThrow(() -> exception(EINVOICE_SIGN_FAILED));

        paymentExecutor.confirmTransfer(approveEInvoiceResponse, null);
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
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(endUserMessage)
                .setMessage(endUserMessage.toString())
                .build();
    }

    private String getIdInHref(String href) {
        String[] token = href.split("%", 2)[0].split("/");
        return token[token.length - 1];
    }
}
