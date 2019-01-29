package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.date.DateUtils;

public class EInvoice extends BaseResponse {
    private PaymentRecipient recipient;
    private HandelsbankenAmount amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;
    private boolean changeAllowed;
    private HandelsbankenSEAccount account;
    private String approvalId;
    // Unused fields
    // private String eInvoiceTicket;
    // private boolean deleteAllowed;

    public String getApprovalId() {
        return approvalId;
    }

    public boolean isChangeAllowed() {
        return changeAllowed;
    }

    public Transfer toTinkTransfer() {
        Transfer transfer = new Transfer();
        transfer.setDueDate(DateUtils.flattenTime(dueDate));
        transfer.setAmount(amount.asAmount());
        transfer.setDestination(recipient.accountIdentifier());

        if (account != null) {
            account.applyTo(transfer);
        }

        transfer.setDestinationMessage(getMessage());
        transfer.setSourceMessage(recipient.getName());

        if (approvalId != null) {
            transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, approvalId);
            transfer.setType(TransferType.EINVOICE);
        } else {
            transfer.setType(TransferType.PAYMENT);
        }

        return transfer;
    }

    public Optional<URL> toEInvoiceDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.EINVOICE_DETAIL);
    }
}
