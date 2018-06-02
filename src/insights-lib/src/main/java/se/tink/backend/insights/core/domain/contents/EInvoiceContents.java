package se.tink.backend.insights.core.domain.contents;

public class EInvoiceContents {
    private String transferId;
    private Double transferAmount;
    private String sourceMessage;

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Double getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Double transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
    }
}
