package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.entities.HandelsbankenSEPaymentDetailRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.date.DateUtils;

public class PaymentDetails extends BaseResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;
    private HandelsbankenAmount amount;
    private HandelsbankenSEPaymentDetailRecipient recipient;
    private HandelsbankenSEAccount account;
    private String approvalId;
    private boolean changeAllowed;


    public boolean isChangeAllowed() {
        return changeAllowed;
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
