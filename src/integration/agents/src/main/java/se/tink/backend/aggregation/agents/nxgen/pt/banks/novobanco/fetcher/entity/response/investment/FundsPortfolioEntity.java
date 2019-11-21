package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.investment;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundsPortfolioEntity {
    @JsonProperty("DescricaoFundo")
    private String fundDescription;

    @JsonProperty("CodigoFundo")
    private String fundCode;

    @JsonProperty("Cotacao")
    private double fundPrice;

    @JsonProperty("QuantidadeTotal")
    private double totalQuantity;

    @JsonProperty("Valorizacao")
    private double appreciation;

    @JsonProperty("QuantidadeDisponivel")
    private double availableQuantity;

    @JsonProperty("QuantidadePendenteLiquidacao")
    private double pendingSettlementQuantity;

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

    public double getFundPrice() {
        return fundPrice;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public double getAppreciation() {
        return appreciation;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public double getPendingSettlementQuantity() {
        return pendingSettlementQuantity;
    }

    public String getPriceDate() {
        return priceDate;
    }

    public String getCurrency() {
        return currency;
    }
}
