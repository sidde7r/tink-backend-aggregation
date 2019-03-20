package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
@XmlRootElement(name = "dato")
public class CreditCardTransactionEntity {
    @JsonProperty("fechaOpera")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("codigoSaldo")
    private String balanceCode;

    @JsonProperty("fechaAnota")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonProperty("movimDia")
    private String dateTransactionNumber;

    @JsonProperty("importeMovto")
    private AmountEntity amount;

    @JsonProperty("descMovimiento")
    private String description;

    @JsonProperty("codMoneda")
    private String currencyCode;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(amount.getTinkAmount())
                .setDate(transactionDate)
                .setDescription(description)
                .build();
    }
}
