package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class CreditCardTransactionEntity {
    private String id;
    private String operationKey;
    private ContractEntity contract;

    // LocalDateTime
    private String transactionDate;

    private String transactionTypeIndicator;
    private BasicEntity concept;
    private AmountEntity amount;
    private HolderAmountEntity holderAmount;
    // LocalDateTime
    private String valueDate;
    private String terminalId;

    private AmountEntity maximumFeeAmount;
    // Seems like Integer, but can't be sure
    private String minimumTimeLimit;
    private String maximumTimeLimit;

    private String virtualTpvIndicator;
    private String operationTypeIndicator;
    private String customizableIndicator;
    private Double exchangeValue;
    private String cardCdepenCode;
    private PurchaserEntity purchaser;
    private AmountEntity commission;
    private ShopEntity shop;
    // LocalDateTime
    private String cardTransactionDate;
    private BasicEntity paymentMethod;
    private BasicEntity humanCategory;
    private BasicEntity humanSubcategory;

    // customPaymentInformation
    private Boolean stillCustomizable;
    private BasicEntity status;
    private Boolean isContractHolder;
    private Boolean visible;
    private String processingType;
    private BasicEntity paymentType;
    private StatementEntity statement;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {

        // TODO: Add info if transaction is pending as we did not see such yet
        return CreditCardTransaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(getBeginDateParsed())
                .setDescription(shop.getName())
                .build();
    }

    @JsonIgnore
    public Date getBeginDateParsed() {
        try {
            return ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITH_TIMEZONE.parse(transactionDate);
        } catch (ParseException pe) {
            throw new RuntimeException("Failed to parse begin date", pe);
        }
    }
}
