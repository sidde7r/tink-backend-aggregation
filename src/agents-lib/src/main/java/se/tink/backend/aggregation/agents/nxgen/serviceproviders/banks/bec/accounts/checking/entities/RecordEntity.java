package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities;

import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class RecordEntity {
    private String date;
    private String dateTxt;
    private String text;
    private String interestFromDate;
    private String interestFromDateTxt;
    private Double amount;
    private String amountTxt;
    private Double balance;
    private String balanceTxt;
    private Boolean isMatched;
    private String recordId;
    private String urlDetail;
    private String accountId;
    private String accountName;
    private String paramMatch;
    private String paramUnmatch;
    private String mainCategoryId;
    private String mainCategoryText;
    private String mainCategoryColor;
    private String subCategoryId;
    private String subCategoryText;
    private String subCategoryColor;
    private String recordGroup;

    public String getDate() {
        return date;
    }

    public String getDateTxt() {
        return dateTxt;
    }

    public String getText() {
        return text;
    }

    public String getInterestFromDate() {
        return interestFromDate;
    }

    public String getInterestFromDateTxt() {
        return interestFromDateTxt;
    }

    public Double getAmount() {
        return amount;
    }

    public String getAmountTxt() {
        return amountTxt;
    }

    public Double getBalance() {
        return balance;
    }

    public String getBalanceTxt() {
        return balanceTxt;
    }

    public Boolean getMatched() {
        return isMatched;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getUrlDetail() {
        return urlDetail;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getParamMatch() {
        return paramMatch;
    }

    public String getParamUnmatch() {
        return paramUnmatch;
    }

    public String getMainCategoryId() {
        return mainCategoryId;
    }

    public String getMainCategoryText() {
        return mainCategoryText;
    }

    public String getMainCategoryColor() {
        return mainCategoryColor;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public String getSubCategoryText() {
        return subCategoryText;
    }

    public String getSubCategoryColor() {
        return subCategoryColor;
    }

    public String getRecordGroup() {
        return recordGroup;
    }

    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setAmount(Amount.inDKK(amount))
                    .setDate(ThreadSafeDateFormat.FORMATTER_DAILY_COMPACT.parse(date))
                    .setDescription(text)
                    .setPending(false)
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
