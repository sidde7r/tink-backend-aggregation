package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class TransactionEntity {
    private static final Joiner DESCRIPTION_JOINER = Joiner.on(' ');

    private String suppIndex;
    private String type;
    private List<String> description;
    private DateValueEntity chargeDate;
    private DoubleValueEntity amount;
    private ExtendedTransactionDetailsEntity extendedTransactionDetails;
    private ForeignTransactionDetailsEntity foreignTransactionDetails;
    private String category;
    private String subcategory;
    private String transTypeDesc;
    private String formattedAmount;

    public String getSuppIndex() {
        return suppIndex;
    }

    public void setSuppIndex(String suppIndex) {
        this.suppIndex = suppIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public DateValueEntity getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(DateValueEntity chargeDate) {
        this.chargeDate = chargeDate;
    }

    public DoubleValueEntity getAmount() {
        return amount;
    }

    public void setAmount(DoubleValueEntity amount) {
        this.amount = amount;
    }

    public ExtendedTransactionDetailsEntity getExtendedTransactionDetails() {
        return extendedTransactionDetails;
    }

    public void setExtendedTransactionDetails(
            ExtendedTransactionDetailsEntity extendedTransactionDetails) {
        this.extendedTransactionDetails = extendedTransactionDetails;
    }

    public ForeignTransactionDetailsEntity getForeignTransactionDetails() {
        return foreignTransactionDetails;
    }

    public void setForeignTransactionDetails(
            ForeignTransactionDetailsEntity foreignTransactionDetails) {
        this.foreignTransactionDetails = foreignTransactionDetails;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getTransTypeDesc() {
        return transTypeDesc;
    }

    public void setTransTypeDesc(String transTypeDesc) {
        this.transTypeDesc = transTypeDesc;
    }

    public Transaction toTransaction() throws ParseException {
        Transaction t = new Transaction();

        if (extendedTransactionDetails != null
                && !Strings.isNullOrEmpty(extendedTransactionDetails.getMerchantName())) {
            t.setDescription(extendedTransactionDetails.getMerchantName());
        } else {
            t.setDescription(DESCRIPTION_JOINER.join(description));
        }

        t.setAmount(-amount.getRawValue());
        t.setDate(
                DateUtils.flattenTime(
                        ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(
                                Long.toString(chargeDate.getRawValue()))));

        if (amount.getRawValue() > 0) {
            t.setType(TransactionTypes.CREDIT_CARD);
        }

        // Re-write swedish description "Betalning mottagen, Tack" to "Betalning"
        if (t.getDescription().toLowerCase().startsWith("betalning mottagen")) {
            t.setDescription("Betalning");
        }

        return t;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }

    public void setFormattedAmount(String formattedAmount) {
        this.formattedAmount = formattedAmount;
    }
}
