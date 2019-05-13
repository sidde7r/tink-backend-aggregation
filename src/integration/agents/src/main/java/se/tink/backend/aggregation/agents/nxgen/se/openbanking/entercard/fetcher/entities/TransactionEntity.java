package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String maskedCardNo;
    private String movementType;
    private String timeOfPurchase;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionPostedDate;

    private Number transactionAmount;
    private Number transactionID;
    private Boolean canBeSplitted;
    private String billingCurrency;
    private Number billingAmount;
    private String transactionCurrency;
    private String merchantId;
    private String mccCode;
    private String merchantCategoryDescription;
    private String merchantName;
    private String merchantCity;
    private String merchantCountry;
    private String accountLevelTransactionDescription;
    private String movementStatus;
    private String reasonCode;
    private String terminalId;

    public Transaction constructCreditCardTransaction() {
        return CreditCardTransaction.builder()
                .setPending(Transactions.OPEN.equalsIgnoreCase(movementStatus))
                .setExternalId(String.valueOf(transactionID))
                .setDate(transactionPostedDate)
                .setDescription(accountLevelTransactionDescription)
                .setAmount(new Amount(billingCurrency, billingAmount))
                .build();
    }
}
