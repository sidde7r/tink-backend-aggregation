package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.einvoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.NordeaExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.transfer.ApproveEInvoiceExecutor;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.enums.TransferPayloadType;
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
        // fetch einvoice to approve
        PaymentEntity eInvoice = getEInvoice(transfer);
        // approve einvoice
        executeApproveEInvoice(eInvoice.getId());
    }

    private void executeApproveEInvoice(String eInvoiceId) {
        // confirm einvoice
        executorHelper.confirm(eInvoiceId);
    }

    // find the EInvoice that matches with transfer to approve
    private PaymentEntity getEInvoice(Transfer transfer) {
        return apiClient.fetchPayments().getPayments().stream()
                .filter(PaymentEntity::isEInvoice)
                .filter(PaymentEntity::isUnconfirmed)
                .filter(eInvoiceEntity -> isEInvoiceEqualsTransfer(transfer, eInvoiceEntity))
                .findFirst()
                .orElseThrow(() -> executorHelper.eInvoiceFailedError());
    }

    private boolean isEInvoiceEqualsTransfer(Transfer transfer, PaymentEntity paymentEntity) {
        return paymentEntity
                .getId()
                .equals(transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID));
    }
}
