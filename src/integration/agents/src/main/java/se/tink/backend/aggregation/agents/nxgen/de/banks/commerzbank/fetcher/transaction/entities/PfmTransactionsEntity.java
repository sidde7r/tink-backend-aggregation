package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PfmTransactionsEntity {
    private TransactionAmount amount;
    private String accountId;
    private NewOriginalAmount newOriginalAmount;
    private String originalAmount;
    private String amountInCurrency;
    private String balance;
    private String bankId;
    private Object categoryChangedTime;
    private int categoryId;
    private Object changedByRule;
    private Object changedByRuleTime;
    private Object comment;
    private Object counterpartyAccountIdentifier;
    private int dataFormat;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private Object detectedCategories;
    private String dueDate;
    private boolean hasUncertainCategorization;
    private boolean hasUserClearedCategoryUncertainty;
    private String id;
    private String insertTime;
    private Object lastModifiedTime;
    private int mcc;
    private String merchantId;
    private String originalDate;
    private String originalText;
    private String parentIdentifier;
    private List<String> parsedData;
    private Object tags;
    private String text;
    private String timestamp;
    private CategoryEntity category;
    private String typeDescription;
    private String typeKey;
    private Object creditCardStatement;
    private String gvo;
    private boolean flagged;
    private boolean read;
    private boolean splitChild;
    private boolean uncleared;
    private String type;

    public TransactionAmount getAmount() {
        return amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public NewOriginalAmount getNewOriginalAmount() {
        return newOriginalAmount;
    }

    public String getOriginalAmount() {
        return originalAmount;
    }

    public String getAmountInCurrency() {
        return amountInCurrency;
    }

    public String getBalance() {
        return balance;
    }

    public String getBankId() {
        return bankId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getDataFormat() {
        return dataFormat;
    }

    public Date getDate() {
        return date;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isHasUncertainCategorization() {
        return hasUncertainCategorization;
    }

    public boolean isHasUserClearedCategoryUncertainty() {
        return hasUserClearedCategoryUncertainty;
    }

    public String getId() {
        return id;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public int getMcc() {
        return mcc;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getOriginalDate() {
        return originalDate;
    }

    public String getOriginalText() {
        return StringEscapeUtils.unescapeHtml4(originalText);
    }

    private String getDescription() {
        return fixDescriptions(getOriginalText());
    }

    /*
        Example of transaction: Kantine Deluxe           Berlin      000
        Removing everything after the spaces, since it is not important
     */
    private String fixDescriptions(String text) {
        if (text.contains(CommerzbankConstants.MULTIPLE_SPACES)) {
            return text.substring(0, text.indexOf(CommerzbankConstants.MULTIPLE_SPACES)).trim();
        }
        return text;
    }

    public String getParentIdentifier() {
        return parentIdentifier;
    }

    public List<String> getParsedData() {
        return parsedData;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public String getGvo() {
        return gvo;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isSplitChild() {
        return splitChild;
    }

    public boolean isUncleared() {
        return uncleared;
    }

    public String getType() {
        return type;
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setAmount(new Amount(getAmount().getCurrency(), getAmount().getValue()))
                .setDate((getDate()))
                .setDescription(getDescription())
                .setPending(isUncleared()).build();
    }

    public Object getCategoryChangedTime() {
        return categoryChangedTime;
    }

    public Object getChangedByRule() {
        return changedByRule;
    }

    public Object getChangedByRuleTime() {
        return changedByRuleTime;
    }

    public Object getComment() {
        return comment;
    }

    public Object getCounterpartyAccountIdentifier() {
        return counterpartyAccountIdentifier;
    }

    public Object getDetectedCategories() {
        return detectedCategories;
    }

    public Object getLastModifiedTime() {
        return lastModifiedTime;
    }

    public Object getTags() {
        return tags;
    }

    public Object getCreditCardStatement() {
        return creditCardStatement;
    }
}
