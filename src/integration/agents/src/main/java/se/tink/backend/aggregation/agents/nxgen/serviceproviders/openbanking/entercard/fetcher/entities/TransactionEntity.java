package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.google.api.client.util.Strings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
public class TransactionEntity {

    @JsonProperty("_links")
    private LinksEntity links;

    /** Example from docs is 2019-08-01T20:10:05, but actual format we get is 2019-08-01 20:10:05 */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate timeOfPurchase;

    /** Example from docs is 2019-08-01T20:10:05, but actual format we get is 2019-08-01 20:10:05 */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate transactionPostedDate;

    private String maskedCardNo;
    private String movementType;
    private BigDecimal transactionAmount;
    private BigDecimal transactionID;
    private Boolean canBeSplitted;
    private String billingCurrency;
    private BigDecimal billingAmount;
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
        Builder builder =
                CreditCardTransaction.builder()
                        .setAmount(ExactCurrencyAmount.of(billingAmount, billingCurrency))
                        .setPending(Transactions.OPEN.equalsIgnoreCase(movementStatus))
                        .setDate(timeOfPurchase)
                        .setDescription(getDescription())
                        .setTransactionDates(getTinkTransactionDates())
                        .setMerchantCategoryCode(mccCode)
                        .setMerchantName(merchantName)
                        .setProprietaryFinancialInstitutionType(movementType);

        if (Objects.nonNull(transactionID)) {
            // TransactionID is marked as required in docs but adding null check just to be safe.
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                    transactionID.toString());
        }

        return (CreditCardTransaction) builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (Objects.nonNull(transactionPostedDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(transactionPostedDate));
        }

        return builder.build();
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
