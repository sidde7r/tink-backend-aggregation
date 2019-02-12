package se.tink.backend.aggregation.agents.abnamro.client.model.creditcards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.backend.aggregation.agents.abnamro.client.model.AmountEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private boolean billed;

    private AmountEntity billingAmount;

    private String description;

    private String lastFourDigits;

    private String merchantDescription;

    private String type;

    private Date date;

    public boolean getBilled() {
        return billed;
    }

    public void setBilled(boolean billed) {
        this.billed = billed;
    }

    public AmountEntity getBillingAmount() {
        return billingAmount;
    }

    public void setBillingAmount(AmountEntity billingAmount) {
        this.billingAmount = billingAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getMerchantDescription() {
        return merchantDescription;
    }

    public void setMerchantDescription(String merchantDescription) {
        this.merchantDescription = merchantDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
