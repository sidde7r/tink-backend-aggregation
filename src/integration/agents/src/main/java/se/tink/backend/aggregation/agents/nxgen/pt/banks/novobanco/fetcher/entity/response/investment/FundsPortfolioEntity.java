package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundsPortfolioEntity {
    @JsonProperty("DescricaoFundo")
    private String fundDescription;

    @JsonProperty("CodigoFundo")
    private String fundCode;

    @JsonProperty("Cotacao")
    private BigDecimal fundPrice;

    @JsonProperty("QuantidadeTotal")
    private BigDecimal totalQuantity;

    @JsonProperty("Valorizacao")
    private BigDecimal appreciation;

    @JsonProperty("QuantidadeDisponivel")
    private BigDecimal availableQuantity;

    @JsonProperty("QuantidadePendenteLiquidacao")
    private BigDecimal pendingSettlementQuantity;

    @JsonProperty("DataCotacao")
    private String priceDate;

    @JsonProperty("Moeda")
    private String currency;

    public String getFundDescription() {
        return fundDescription;
    }

    public String getFundCode() {
        return fundCode;
    }

    public BigDecimal getFundPrice() {
        return fundPrice;
    }

    public BigDecimal getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getAppreciation() {
        return appreciation;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getPendingSettlementQuantity() {
        return pendingSettlementQuantity;
    }

    public String getPriceDate() {
        return priceDate;
    }

    public String getCurrency() {
        return currency;
    }
}
