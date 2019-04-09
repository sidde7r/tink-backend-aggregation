package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import java.util.Date;
import se.tink.libraries.date.DateUtils;

public class BillRequest {
    private String amount;
    private String reference;
    private String bankGiro;
    private String saveReceiver;
    private String saveForLaterApproval;
    private String date;
    private String fromAccountId;
    private String toAccountId;
    private String receiverText;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getBankGiro() {
        return bankGiro;
    }

    public void setBankGiro(String bankGiro) {
        this.bankGiro = bankGiro;
    }

    public String getSaveReceiver() {
        return saveReceiver;
    }

    public void setSaveReceiver(String saveReceiver) {
        this.saveReceiver = saveReceiver;
    }

    public String getSaveForLaterApproval() {
        return saveForLaterApproval;
    }

    public void setSaveForLaterApproval(String saveForLaterApproval) {
        this.saveForLaterApproval = saveForLaterApproval;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public void setDate(Date date) {
        if (date == null) {
            this.date =
                    "\\/Date("
                            + DateUtils.getNextBusinessDay().getTime() / 1000 * 1000
                            + "+0200)\\/";
        } else {
            this.date = "\\/Date(" + date.getTime() / 1000 * 1000 + "+0200)\\/";
        }
    }

    public String getReceiverText() {
        return receiverText;
    }

    public void setReceiverText(String receiverText) {
        this.receiverText = receiverText;
    }
}
