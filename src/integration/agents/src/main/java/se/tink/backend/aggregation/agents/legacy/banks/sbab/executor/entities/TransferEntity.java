package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public boolean hasPendingStatus() {
        return SBABConstants.TransferStatus.PENDING.equalsIgnoreCase(transferStatus);
    }
}
