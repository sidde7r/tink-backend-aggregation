package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class QualificationEntity {

    @JsonProperty("descripcionEmision")
    private String tickerCode;

    @JsonProperty("identificadorBolsaMercado")
    private String identifierMarketplaceGroup;

    @JsonProperty("identificadorValor")
    private String identifierValue;

    @JsonProperty("mercado")
    private String market;

    @JsonProperty("isin")
    private String isin;

    @JsonProperty("numeroDeposito")
    private String depositNumber;

    @JsonProperty("participaciones")
    private Double quantity;

    @JsonProperty("tipoActivo")
    private String type;

    @JsonProperty("descripcionTipoActivo")
    private String typeDescription;

    @JsonProperty("valoracionTotal")
    private AmountEntity valuationTotal;

    @JsonProperty("valoracionUnitaria")
    private AmountEntity valuationUnit;

    @JsonProperty("valoracionTotalEUR")
    private AmountEntity valuationTotalEUR;

    @JsonProperty("valoracionUnitariaEUR")
    private AmountEntity valuationUnitEUR;

    public String getTickerCode() {
        return tickerCode;
    }

    public String getIdentifierMarketplaceGroup() {
        return identifierMarketplaceGroup;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public String getMarket() {
        return market;
    }

    public String getIsin() {
        return isin;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public AmountEntity getValuationTotal() {
        return valuationTotal;
    }

    public AmountEntity getValuationUnit() {
        return valuationUnit;
    }

    public AmountEntity getValuationTotalEUR() {
        return valuationTotalEUR;
    }

    public AmountEntity getValuationUnitEUR() {
        return valuationUnitEUR;
    }
}
