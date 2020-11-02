package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    @JsonUnwrapped private BalanceEntity amount;

    // These are used to get transaction details. I'm not sure how to translate them.
    private String indComunicados;
    private String accesoDetalleMov;
    private String refValConsultaCom;

    @JsonProperty("concepto")
    private String description;

    private LocalDate date;

    @JsonProperty("fechaValor")
    private void setDate(Map<String, String> node) {

        String dateString = node.get("valor");
        String dateFormat = node.get("formato");

        date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndComunicados() {
        return indComunicados;
    }

    public String getAccesoDetalleMov() {
        return accesoDetalleMov;
    }

    public String getRefValConsultaCom() {
        return refValConsultaCom;
    }

    public Transaction toTinkTransaction() {
        Transaction.Builder txBuilder =
                Transaction.builder()
                        .setAmount(amount.toExactCurrencyAmount())
                        .setDescription(description);

        if (!Objects.isNull(date)) {
            txBuilder.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        return txBuilder.build();
    }
}
