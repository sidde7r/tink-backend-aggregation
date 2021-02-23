package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoice {

    private String accepted;
    private double amount;
    private long due;
    private String electronicInvoiceId;
    private String expired;
    private String ocr;
    private Originator originator;

    public String getAccepted() {
        return accepted;
    }

    public double getAmount() {
        return amount;
    }

    public Long getDue() {
        return due;
    }

    public String getElectronicInvoiceId() {
        return electronicInvoiceId;
    }

    public String getExpired() {
        return expired;
    }

    public String getOcr() {
        return ocr;
    }

    public Originator getOriginator() {
        return originator;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDue(long due) {
        this.due = due;
    }

    public void setElectronicInvoiceId(String electronicInvoiceId) {
        this.electronicInvoiceId = electronicInvoiceId;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public void setOriginator(Originator originator) {
        this.originator = originator;
    }

    public Transfer toTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(this.amount));
        transfer.setDestinationMessage(this.ocr);
        transfer.setSourceMessage(this.originator.getName());
        transfer.setType(TransferType.EINVOICE);
        transfer.setDestination(this.originator.generalGetAccountIdentifier());
        transfer.setDueDate(new Date(this.getDue()));
        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, this.getElectronicInvoiceId());

        return transfer;
    }
}
