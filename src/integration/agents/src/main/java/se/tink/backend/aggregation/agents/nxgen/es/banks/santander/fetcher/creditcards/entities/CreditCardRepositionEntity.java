package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class CreditCardRepositionEntity {
    @JsonProperty("codigoSaldo")
    private String balanceCode;
    @JsonProperty("codigoMoneda")
    private String currencyCode;
    @JsonProperty("fechaAnota")
    private String bookingDate;
    @JsonProperty("movimDia")
    private int dateTransactionNumber;
    @JsonProperty("fechaOpera")
    private String transactionDate;
    @JsonProperty("cursorRepos")
    private int repositionCursor;
}
