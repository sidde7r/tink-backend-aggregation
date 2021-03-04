package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    private String maskedCardNo;
    private String movementType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date timeOfPurchase;

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

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setPending(Transactions.OPEN.equalsIgnoreCase(movementStatus))
                .setDate(timeOfPurchase)
                .setDescription(getDescription())
                .setAmount(ExactCurrencyAmount.of(billingAmount, billingCurrency))
                .build();
    }

    /**
     * 'merchantName' is the description that we get in the RE agent, so we first look at this.
     * Sometimes in special cases like 'INBETALNING', 'RÄNTA' etc it is null and therefore we use
     * 'accountLevelTransactionDescription'.
     *
     * @return Transaction description
     */
    @JsonIgnore
    private String getDescription() {
        return Strings.isNullOrEmpty(merchantName)
                ? accountLevelTransactionDescription
                : merchantName;
    }

    /**
     * Transactions have various other movementStatus like 'CANCELLED', 'MATCHED' and these
     * transactions are not shown/retrieved in RE agent.
     *
     * @return if the transaction needs to be included in response or not
     */
    @JsonIgnore
    public boolean isValidTransaction() {
        return StringUtils.equalsIgnoreCase(movementStatus, "OTHER");
    }
}
