package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import java.util.List;

public class ExtendedTransactionDetailsEntity {
    private DateValueEntity processDate;
    private String merchantName;
    private List<String> address;

    public DateValueEntity getProcessDate() {
        return processDate;
    }

    public void setProcessDate(DateValueEntity processDate) {
        this.processDate = processDate;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}
