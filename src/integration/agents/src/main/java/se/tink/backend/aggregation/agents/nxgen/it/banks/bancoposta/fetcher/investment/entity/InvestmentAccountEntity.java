package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentAccountEntity {

    @JsonProperty("descrizione")
    private String description;

    @JsonProperty("numerica")
    private String id;

    @JsonProperty("valoreRimborsoLordo")
    private BigDecimal currentValue;
}
