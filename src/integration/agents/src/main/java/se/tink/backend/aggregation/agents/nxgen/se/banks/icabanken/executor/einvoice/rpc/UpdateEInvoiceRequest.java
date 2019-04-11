package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class UpdateEInvoiceRequest {
    @JsonIgnore
    private static final DecimalFormat AMOUNT_FORMAT =
            new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    @JsonProperty("PayDate")
    private String payDate;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("InvoiceId")
    private String invoiceId;

    private UpdateEInvoiceRequest(Date dueDate, Amount amount, String invoiceId) {
        this.payDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate);
        this.amount = AMOUNT_FORMAT.format(amount.getValue());
        this.invoiceId = invoiceId;
    }

    public static UpdateEInvoiceRequest create(Date dueDate, Amount amount, String invoiceId) {
        return new UpdateEInvoiceRequest(dueDate, amount, invoiceId);
    }

    public String getPayDate() {
        return payDate;
    }

    public String getAmount() {
        return amount;
    }

    public String getInvoiceId() {
        return invoiceId;
    }
}
