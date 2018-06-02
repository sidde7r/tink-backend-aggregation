package se.tink.backend.connector.rpc.abnamro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    public final static String USER_INITIATED_ORIGIN = "C";
    public final static String BATCH_ORIGIN = "B";
    public final static String COMPRESSED_BOOKING_ORIGIN = "U";


    private double amount;
    private String cpAccount;
    private String cpName;
    private Date date;
    private List<String> description;
    private Map<String, Object> payload;
    private String type;
    private String origin;
    private String currency;
    private String originType;

    private Date entityCreated;

    public TransactionEntity() {
        entityCreated = new Date();
    }

    public double getAmount() {
        return amount;
    }

    public String getCpAccount() {
        return cpAccount;
    }
    
    public String getCpName() {
        return cpName;
    }
    
    public Date getDate() {
        return date;
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getType() {
        return type;
    }

    public String getOriginType() {
        return originType;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setCpAccount(String cpAccount) {
        this.cpAccount = cpAccount;
    }

    public void setCpName(String cpName) {
        this.cpName = cpName;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOriginType(String originType) {
        this.originType = originType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("amount", getAmount())
                .add("cpName", getCpName())
                .add("date", getDate())
                .add("description", getDescription())
                .toString();
    }

    public Date getEntityCreated() {
        return entityCreated;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
