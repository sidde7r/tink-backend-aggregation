package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.einvoice;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.PaymentAccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.NordeaExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.utilities.NordeaAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.NordeaEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * In the Nordea app a user can change amount and due date of the e-invoice. In that case the
 * eInvoice changes type to bankgiro payment instead with a new ID.
 */
public class NordeaApproveEInvoiceExecutor implements ApproveEInvoiceExecutor {
    private static final Logger log = LoggerFactory.getLogger(NordeaApproveEInvoiceExecutor.class);
    private final Catalog catalog;
    private NordeaSEApiClient apiClient;
    private NordeaExecutorHelper executorHelper;

    public NordeaApproveEInvoiceExecutor(
            NordeaSEApiClient apiClient, Catalog catalog, NordeaExecutorHelper executorHelper) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.executorHelper = executorHelper;
    }

    @Override
    public void approveEInvoice(Transfer transfer) throws TransferExecutionException {
        // disable e-invoice functionality
        throw executorHelper.eInvoiceNotFoundError();
    }

    private boolean isModifyingEInvoice(PaymentEntity eInvoice, Transfer transfer) {
        return !eInvoice.isEqualToTransfer(transfer);
    }

    private PaymentEntity updateEInvoice(PaymentEntity eInvoice, Transfer transfer) {
        validateCanBeUpdated(eInvoice, transfer);
        PaymentRequest updateRequest = createUpdateRequest(eInvoice, transfer);
        return apiClient.updatePayment(updateRequest);
    }

    private PaymentRequest createUpdateRequest(PaymentEntity eInvoice, Transfer transfer) {
        PaymentRequest request = new PaymentRequest();
        request.setId(eInvoice.getId());
        request.setReference(eInvoice.getReference());
        request.setBankName(eInvoice.getBankName());
        request.setOwnMessage(eInvoice.getOwnMessage());
        request.setFrom(transfer.getSource().getIdentifier(new NordeaAccountIdentifierFormatter()));
        request.setFromAccountNumberType(PaymentAccountTypes.LBAN);
        request.setTo(transfer.getDestination().getIdentifier());
        request.setRecipientName(
                transfer.getDestination().getName().orElse(eInvoice.getRecipientName()));
        request.setType(executorHelper.getPaymentType(transfer.getDestination()));
        request.setToAccountNumberType(
                executorHelper.getPaymentAccountType(transfer.getDestination()));
        request.setAmount(transfer.getAmount());
        request.setDue(transfer.getDueDate());
        request.setMessage(transfer.getDestinationMessage());
        return request;
    }

    private void validateCanBeUpdated(PaymentEntity eInvoice, Transfer transfer) {
        if (!eInvoice.getPermissions().canModifyAmount()
                && !transfer.getAmount().equals(eInvoice.getAmount())) {
            throw executorHelper.eInvoiceUpdateAmountNotAllowed();
        }
        if (!eInvoice.getPermissions().canModifyDue()
                && !DateUtils.isSameDay(transfer.getDueDate(), eInvoice.getDue())) {
            throw executorHelper.eInvoiceUpdateDueNotAllowed();
        }
        if (!eInvoice.getPermissions().canModifyMessage()
                && !Strings.nullToEmpty(transfer.getDestinationMessage())
                        .equals(Strings.nullToEmpty(eInvoice.getMessage()))) {
            throw executorHelper.eInvoiceUpdateMessageNotAllowed();
        }
        if (!eInvoice.getPermissions().canModifyTo()
                && !eInvoice.getRecipientAccountNumber()
                        .equals(transfer.getDestination().getIdentifier())) {
            throw executorHelper.eInvoiceUpdateToNotAllowed();
        }
        AccountIdentifierFormatter nordeaFormatter = new NordeaAccountIdentifierFormatter();
        if (!eInvoice.getPermissions().canModifyFrom()
                && !transfer.getSource()
                        .getIdentifier(nordeaFormatter)
                        .equals(eInvoice.getFrom())) {
            throw executorHelper.eInvoiceUpdateFromNotAllowed();
        }
    }

    private void executeApproveEInvoice(String eInvoiceId) {
        // confirm einvoice
        executorHelper.confirm(eInvoiceId);
    }

    // find the EInvoice that matches with transfer to approve
    private PaymentEntity getEInvoice(String transferId) {
        return new NordeaEInvoiceFetcher(apiClient)
                .fetchAsPaymentStream()
                .filter(eInvoiceEntity -> isEInvoiceEqualsTransfer(transferId, eInvoiceEntity))
                .findFirst()
                .orElseThrow(() -> executorHelper.eInvoiceNotFoundError());
    }

    private boolean isEInvoiceEqualsTransfer(String transferId, PaymentEntity paymentEntity) {
        return paymentEntity.getApiIdentifier().equals(transferId);
    }
}
