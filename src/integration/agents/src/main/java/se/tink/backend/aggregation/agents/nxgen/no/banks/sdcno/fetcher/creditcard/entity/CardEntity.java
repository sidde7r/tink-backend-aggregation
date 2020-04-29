package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardEntity {
    @JsonProperty("kortnummer")
    private String cardNumber;

    @JsonProperty("kontonummer")
    private String accountId;

    @JsonProperty("produktnavn")
    private String cardName;

    @JsonProperty("saldo")
    private String amount;

    @JsonProperty("kredittgrense")
    private String availableAmount;

    private String type;

    private String status;
}
