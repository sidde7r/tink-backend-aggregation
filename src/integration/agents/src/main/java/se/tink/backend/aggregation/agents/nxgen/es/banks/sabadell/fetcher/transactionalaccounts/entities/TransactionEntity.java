package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date date;

    private String concept;
    private AmountEntity amount;
    private boolean canSplit;
    private String cardNumber;
    private AmountEntity balance;
    private boolean existDocument;
    private String apuntNumber;
    private String productCode;
    private String valueDate;
    private String conceptCode;
    private String conceptDetail;
    private String timeStamp;
    private String referencor;
    private String sessionDate;
    private String returnBillCode;
    private String numPAN;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.parseToTinkAmount())
                .setDate(date)
                .setDescription(concept)
                .build();
    }

    public Date getDate() {
        return date;
    }

    public String getConcept() {
        return concept;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public boolean isCanSplit() {
        return canSplit;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public boolean isExistDocument() {
        return existDocument;
    }

    public String getApuntNumber() {
        return apuntNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public String getConceptDetail() {
        return conceptDetail;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getReferencor() {
        return referencor;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public String getReturnBillCode() {
        return returnBillCode;
    }

    public String getNumPAN() {
        return numPAN;
    }
}
