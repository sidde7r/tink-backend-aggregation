package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEInvoiceRequest {
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    @JsonProperty("PayDate")
    private String payDate;
    @JsonProperty("Amount")
    private String amount;
    @JsonProperty("InvoiceId")
    private String invoiceId;

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setFormattedAmount(Double amount) {
        setAmount(AMOUNT_FORMAT.format(amount));
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
