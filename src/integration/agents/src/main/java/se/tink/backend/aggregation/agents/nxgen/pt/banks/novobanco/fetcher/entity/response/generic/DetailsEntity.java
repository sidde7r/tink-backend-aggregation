package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    @JsonProperty("Produto")
    private String product;

    @JsonProperty("Contrato")
    private String contract;

    @JsonProperty("Saldo")
    private BigDecimal balance;

    public String getProduct() {
        return product;
    }

    public String getContract() {
        return contract;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
