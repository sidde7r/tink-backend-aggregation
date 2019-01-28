package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.DetailedPermissions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.entities.HandelsbankenSEPaymentDetailRecipient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces.UpdatablePayment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.date.DateUtils;

public class PaymentDetails extends BaseResponse implements UpdatablePayment {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;
    private HandelsbankenAmount amount;
    private HandelsbankenSEPaymentDetailRecipient recipient;
    private HandelsbankenSEAccount account;
    private String approvalId;
    private boolean changeAllowed;
    private DetailedPermissions detailedPermissions;
    private HandelsbankenSEPaymentContext context;

    public DetailedPermissions getDetailedPermissions() {
        return detailedPermissions;
    }

    public boolean isChangeAllowed() {
        return changeAllowed;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public HandelsbankenSEPaymentContext getContext() {
        return context;
    }

    public URL toPaymentContext() {
        return findLink(HandelsbankenConstants.URLS.Links.PAYMENT_CONTEXT);
    }

    public Optional<URL> toUpdate() {
        return searchLink(HandelsbankenConstants.URLS.Links.UPDATE);
    }

    public Optional<URL> toSignature () {
        return searchLink(HandelsbankenConstants.URLS.Links.SIGNATURE);
    }

    public Optional<URL> toEInvoiceDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.EINVOICE_DETAIL);
    }

    public Transfer toTransfer() {
        Transfer transfer = new Transfer();

        transfer.setDueDate(DateUtils.flattenTime(dueDate));
        transfer.setAmount(Amount.inSEK(amount.asDouble()));
        recipient.applyTo(transfer);

        if (account != null) {
            account.applyTo(transfer);
        }

        transfer.setDestinationMessage(getMessage());

        if (approvalId != null) {
            transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, approvalId);
            transfer.setType(TransferType.EINVOICE);
        } else {
            transfer.setType(TransferType.PAYMENT);
        }

        return transfer;
    }
}
