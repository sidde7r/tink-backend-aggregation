package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.aggregation.agents.banks.danskebank.DanskeUtils;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * { "amount": "1.00", "receipt": "false", "fromAccountId": "3419584517", "saveForLaterApproval":
 * "true", "date": "\/Date(1464220800000+0200)\/" }
 */
public class EInvoiceApproveRequest {
    private String amount;
    private String receipt;
    private String fromAccountId;
    private String saveForLaterApproval;
    private String date;

    public EInvoiceApproveRequest(String fromAccountId, Transfer transfer) {
        setFromAccountId(fromAccountId);
        setAmount(transfer.getAmount().getValue());
        setDate(transfer.getDueDate());
        setReceipt(false);
        setSaveForLaterApproval(false);
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        DecimalFormat decimalFormat = new DecimalFormat("0.00", symbols);
        this.amount = decimalFormat.format(amount);
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(boolean receipt) {
        this.receipt = Boolean.toString(receipt);
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getSaveForLaterApproval() {
        return saveForLaterApproval;
    }

    public void setSaveForLaterApproval(Boolean saveForLaterApproval) {
        this.saveForLaterApproval = saveForLaterApproval.toString();
    }

    public String getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = DanskeUtils.formatDanskeDateDaily(date);
    }
}
