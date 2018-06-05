package se.tink.backend.system.rpc;

import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.utils.StringUtils;

public class TransactionPart {
    private BigDecimal amount;
    private String categoryId;
    private String counterpartId;
    private String counterpartTransactionId;
    private Date date;
    private String id;
    private Date lastModified;

    public void setCounterpart(String counterpartId, String counterpartTransactionId) {
        this.counterpartId = counterpartId;
        this.counterpartTransactionId = counterpartTransactionId;
        this.lastModified = new Date();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCounterpartId() {
        return counterpartId;
    }

    public void setCounterpartId(String counterpartId) {
        this.counterpartId = counterpartId;
    }

    public String getCounterpartTransactionId() {
        return counterpartTransactionId;
    }

    public void setCounterpartTransactionId(String counterpartTransactionId) {
        this.counterpartTransactionId = counterpartTransactionId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        generateIdIfMissing();
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    private void generateIdIfMissing() {
        if (id == null) {
            id = StringUtils.generateUUID();
        }
    }

    @Override
    public String toString() {
        generateIdIfMissing();
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("amount", amount)
                .add("date", date)
                .add("counterpartId", counterpartId)
                .add("counterpartTransactionId", counterpartTransactionId)
                .toString();
    }
}
