package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {

    private static final Locale SE_LOCALE = new Locale("sv", "SE");
    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(SE_LOCALE);
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.##", DECIMAL_FORMAT_SYMBOLS);

    private String amount;
    private PaymentReference reference;
    private String einvoiceReference;
    private String date;
    private String fromAccountId;
    private String recipientId;
    private String noteToSender;
    private String type;

    public String getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getType() {
        return type;
    }

    public PaymentReference getReference() {
        return reference;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setFormattedAmount(Double amount) {
        setAmount(AMOUNT_FORMAT.format(amount));
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setReference(PaymentReference reference) {
        this.reference = reference;
    }

    public String getNoteToSender() {
        return noteToSender;
    }

    public void setNoteToSender(String noteToSender) {
        this.noteToSender = noteToSender;
    }

    public void setFormattedDueDate(Date date, Catalog catalog) throws TransferExecutionException {
        if (date != null) {

            // To make the payment within 2 business days, leave the due date empty.
            Date nextBusinessDay = DateUtils.getNextBusinessDay();
            if (date.after(DateUtils.setInclusiveEndTime(nextBusinessDay))) {

                if (!DateUtils.isBusinessDayWithinDaysFromNow(date, 365)) {
                    throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                            .setEndUserMessage(catalog.getString("Payment date has to be within one year from now"))
                            .setMessage(String.format("DueDate was set to more than 1 year in the future: %s", date))
                            .build();
                }

                setDate(ThreadSafeDateFormat.FORMATTER_DAILY.format(date));
            }
        }
    }

    public String getEinvoiceReference() {
        return einvoiceReference;
    }

    public void setEinvoiceReference(String einvoiceReference) {
        this.einvoiceReference = einvoiceReference;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}
