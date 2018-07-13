package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientEntity implements GeneralAccountEntity {
    static final Pattern PATTERN_BG_RECIPIENT = Pattern.compile(".*\\d{3,4}-\\d{4}");
    static final Pattern PATTERN_PG_RECIPIENT = Pattern.compile(".*\\d{1,6}-\\d");

    private String giroNumber;
    private String name;
    private String ocrType;
    private String paymentType;
    private String reference;
    private boolean modificationAllowed;
    private String invoiceId;
    private String debitDate;

    public String getGiroNumber() {
        return giroNumber;
    }

    public void setGiroNumber(String giroNumber) {
        this.giroNumber = giroNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOcrType() {
        return ocrType;
    }

    public void setOcrType(String ocrType) {
        this.ocrType = ocrType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isModificationAllowed() {
        return modificationAllowed;
    }

    public void setModificationAllowed(boolean modificationAllowed) {
        this.modificationAllowed = modificationAllowed;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        Matcher matcher = PATTERN_BG_RECIPIENT.matcher(giroNumber);
        if (matcher.matches()) {
            return new BankGiroIdentifier(giroNumber);
        }

        matcher = PATTERN_PG_RECIPIENT.matcher(giroNumber);
        if (matcher.matches()) {
            return new PlusGiroIdentifier(giroNumber);
        }

        throw new RuntimeException ("unknown giro number: " + giroNumber);
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return name;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(String debitDate) {
        this.debitDate = debitDate;
    }
}
