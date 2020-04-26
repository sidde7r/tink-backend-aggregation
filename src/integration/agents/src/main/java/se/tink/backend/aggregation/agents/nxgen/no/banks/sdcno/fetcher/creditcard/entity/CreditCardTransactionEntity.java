package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardTransactionEntity {
    @JsonProperty("posteringTekst")
    private String description;

    @JsonProperty("transaksjonsDato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("registreringsDato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookedTransactionDate;

    @JsonProperty("overfortBelop")
    private String transferedAmount;

    @JsonProperty("originalBelop")
    private String originalAmount;

    @JsonProperty("overfortValutaIsoKode")
    private String currency;
}
