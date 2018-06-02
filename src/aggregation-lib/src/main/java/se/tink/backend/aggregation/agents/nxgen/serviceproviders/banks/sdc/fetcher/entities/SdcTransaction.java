package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.ToStringBuilder;

/*
    Data:
"paymentDate": "2017-07-03",
"amount": {
    SdcAmount
},
"entityKey": {
    SdcTransactionEntityKey
},
"balance": {
    SdcAmount
},
"label": "Avgift",
"originalText": "",
"clearingChoice": "Unknown",
"icon": null,
"categoryLabel": null,
"subCategoryLabel": null,
"dueDate": "2017-07-01",
"crrfValue": null,
"eerfValue": null

 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SdcTransaction {
    private String paymentDate;
    private SdcAmount amount;
    private SdcAmount balance;
    private SdcTransactionEntityKey entityKey;
    private String label;
    private String originalText;
    private String clearingChoice;
    private String categoryLabel;
    private String subCategoryLabel;
    private String dueDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("paymentDate", paymentDate)
                .append("amount", amount)
                .append("balance", balance)
                .append("entityKey", entityKey)
                .append("label", label)
                .append("originalText", originalText)
                .append("clearingChoice", clearingChoice)
                .append("categoryLabel", categoryLabel)
                .append("subCategoryLabel", subCategoryLabel)
                .append("dueDate", dueDate)
                .toString();
    }

    private String formatDescription(String description, int numberOfCharacters) {
        return description.substring(numberOfCharacters);
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public SdcAmount getAmount() {
        return amount;
    }

    public SdcAmount getBalance() {
        return balance;
    }

    public SdcTransactionEntityKey getEntityKey() {
        return entityKey;
    }

    public String getLabel() {
        return label;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getClearingChoice() {
        return clearingChoice;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getSubCategoryLabel() {
        return subCategoryLabel;
    }
}
