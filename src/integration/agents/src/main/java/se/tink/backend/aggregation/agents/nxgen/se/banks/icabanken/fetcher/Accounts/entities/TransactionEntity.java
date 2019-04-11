package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("Key")
    private String key;

    @JsonProperty("MemoText")
    private String memoText;

    @JsonProperty("PostedDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date postedDate;

    @JsonProperty("PostedDateTime")
    private String postedDateTime;

    @JsonProperty("MerchantType")
    private String merchantType;

    @JsonDouble
    @JsonProperty("Amount")
    private double amount;

    @JsonDouble
    @JsonProperty("AccountBalance")
    private double accountBalance;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("HasDetails")
    private boolean hasDetails;

    @JsonProperty("Details")
    private DetailsEntity details;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inSEK(amount))
                .setDate(postedDate)
                .setDescription(memoText)
                .build();
    }

    public String getKey() {
        return key;
    }

    public String getMemoText() {
        return memoText;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public String getPostedDateTime() {
        return postedDateTime;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public double getAmount() {
        return amount;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public String getType() {
        return type;
    }

    public boolean isHasDetails() {
        return hasDetails;
    }

    public DetailsEntity getDetails() {
        return details;
    }
}
