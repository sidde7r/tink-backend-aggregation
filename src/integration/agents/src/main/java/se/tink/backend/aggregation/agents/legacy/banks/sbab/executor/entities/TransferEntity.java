package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferEntity {
    private String transferId;
    private String valueDate;
    private String accountNumberFrom;
    private String accountNumberTo;
    private String bankNameFrom;
    private String bankNameTo;
    private double amount;
    private String narrativeFrom;
    private String narrativeTo;
    private String transferStatus;
    private boolean futureTransfer;
    private boolean recurringTransfer;
    private String fromAccountName;
    private String toAccountName;
    private static final Logger log = LoggerFactory.getLogger(TransferEntity.class);

    @JsonIgnore
    public boolean hasSuccessStatus() {
        logUnknownStatus();
        return isStatusPending() || isStatusComplete() || isSuccessfulFutureTransfer();
    }

    @JsonIgnore
    private void logUnknownStatus() {
        if (!(isStatusComplete() || (isSuccessfulFutureTransfer()))) {
            log.info("Transfer Status: {}, isFutureTransfer: {}", transferStatus, futureTransfer);
        }
    }

    @JsonIgnore
    private boolean isStatusComplete() {
        return SBABConstants.TransferStatus.COMPLETE.equalsIgnoreCase(transferStatus);
    }

    @JsonIgnore
    private boolean isStatusPending() {
        return SBABConstants.TransferStatus.PENDING.equalsIgnoreCase(transferStatus);
    }

    @JsonIgnore
    private boolean isSuccessfulFutureTransfer() {
        return futureTransfer
                && SBABConstants.TransferStatus.UNKNOWN.equalsIgnoreCase(transferStatus);
    }
}
