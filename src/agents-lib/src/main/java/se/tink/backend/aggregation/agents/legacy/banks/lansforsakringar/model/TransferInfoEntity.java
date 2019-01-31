package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferInfoEntity {
    private String toText;
    private String fromText;
    private String toAccountBankName;
    private String finalDate;
    private boolean transferToAccountInternal;
    private boolean recurring;

    public String getToText() {
        return toText;
    }

    public void setToText(String toText) {
        this.toText = toText;
    }

    public String getFromText() {
        return fromText;
    }

    public void setFromText(String fromText) {
        this.fromText = fromText;
    }

    public String getToAccountBankName() {
        return toAccountBankName;
    }

    public void setToAccountBankName(String toAccountBankName) {
        this.toAccountBankName = toAccountBankName;
    }

    public String getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(String finalDate) {
        this.finalDate = finalDate;
    }

    public boolean isTransferToAccountInternal() {
        return transferToAccountInternal;
    }

    public void setTransferToAccountInternal(boolean transferToAccountInternal) {
        this.transferToAccountInternal = transferToAccountInternal;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
}
