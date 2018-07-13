package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@XmlRootElement
public class TransactionEntity {
    @JsonProperty("fechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;
    @JsonProperty("fechaValor")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;
    @JsonProperty("importe")
    private AmountEntity amount;
    @JsonProperty("tipoMovimiento")
    private String transactionType;
    @JsonProperty("importeSaldo")
    private AmountEntity balance;
    @JsonProperty("diaMovimiento")
    private String transactionDay; // This value is weird, it's always 99999
    @JsonProperty("fechaAnotacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date annotationDate;
    @JsonProperty("numeroDGO")
    private DgoNumberEntity dgoNumberEntity;
    @JsonProperty("descripcion")
    private String description;
    @JsonProperty("numeroMovimiento")
    private String transactionNumber;

    public Date getTransactionDate() {
        return transactionDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getTransactionDay() {
        return transactionDay;
    }

    public Date getAnnotationDate() {
        return annotationDate;
    }

    public DgoNumberEntity getDgoNumberEntity() {
        return dgoNumberEntity;
    }

    public String getDescription() {
        return description;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.getTinkAmount())
                .setDate(transactionDate)
                .setDescription(description).build();
    }
}
