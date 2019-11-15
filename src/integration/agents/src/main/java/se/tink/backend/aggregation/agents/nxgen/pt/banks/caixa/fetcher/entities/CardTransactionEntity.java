package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionEntity {

    private Date bookDate;
    private String cardDescription;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private String description;
    private Boolean fractionedPayment;
    private String maskedCardNumber;
    private String transactionId;
    private Date valueDate;

    public Date getBookDate() {
        return bookDate;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getFractionedPayment() {
        return fractionedPayment;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Date getValueDate() {
        return valueDate;
    }
}
